package com.example.speedcloud.service

import android.app.*
import android.content.Intent
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import android.widget.Toast
import com.ejlchina.okhttps.HTTP
import com.ejlchina.okhttps.gson.GsonMsgConvertor
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.bean.*
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.util.OkHttpUtils
import com.google.gson.Gson
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList


// https://developer.android.google.cn/guide/components/services#CreatingAService
class UploadService : Service() {
    var onProgressChangeListener: RecyclerListener.OnProgressChangeListener? = null
    var onCompleteTransferListener: RecyclerListener.OnCompleteTransferListener? = null
    private val binder = UploadBinder()
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    companion object {
        private var CHUNK_SIZE = 1024 * 1024 * 20
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            val bundle = msg.data
            val message = Gson().fromJson(bundle.getString("message"), ServiceMessage::class.java)
            upload(message.path)
            stopSelf(message.startId)
        }

        /**
         * 上传的总控制
         */
        private fun upload(path: String) {
            MainApplication.getInstance().uploadingState(FileState.CALC)
            onProgressChangeListener?.onProgressChange()
            val file = File(path)
            val chunkMd5 = ArrayList<String>()
            val fileMd5 = calcMd5(file, chunkMd5)
            MainApplication.getInstance().uploadingState(FileState.LOADING)
            onProgressChangeListener?.onProgressChange()
            val quick = checkQuickUpload(file, fileMd5)
            if (!quick) uploadChunk(file, fileMd5, chunkMd5)
        }

        /**
         * 将MessageDigest转为md5
         */
        private fun getMd5(bytes: ByteArray): String {
            // 转为16进制, 不足32位补0
            return BigInteger(1, bytes).toString(16).padStart(32, '0')
        }

        /**
         * 获取文件md5和分片md5
         */
        private fun calcMd5(file: File, chunkMd5: ArrayList<String>): String {
            val messageDigest = MessageDigest.getInstance("md5")
            val chunkMessageDigest = MessageDigest.getInstance("md5")
            val randomAccessFile = RandomAccessFile(file, "r")
            val bytes = ByteArray(CHUNK_SIZE)
            var len = 0
            while (randomAccessFile.read(bytes).also { len = it } != -1) {
                messageDigest.update(bytes, 0, len)
                chunkMessageDigest.update(bytes, 0, len)
                chunkMd5.add(getMd5(chunkMessageDigest.digest()))
            }
            return getMd5(messageDigest.digest())
        }

        /**
         * 上传分片
         */
        private fun uploadChunk(
            file: File,
            fileMd5: String,
            chunkMd5: ArrayList<String>,
        ) {
            val existChunk = ArrayList<Int>()
            if (!getExistChunkArray(fileMd5, chunkMd5.size, existChunk)) return
            val needUpload = Array(chunkMd5.size) { true }
            for (exist in existChunk) needUpload[exist] = false
            val randomAccessFile = RandomAccessFile(file, "r")
            val chunk = ByteArray(CHUNK_SIZE)
            var befTimeStamp: Long = 0
            for (i in chunkMd5.indices) {
                if (needUpload[i]) {
                    Log.d("hgf", "当前分块：${i + 1}/${chunkMd5.size}")
                    randomAccessFile.seek(i.toLong() * CHUNK_SIZE)
                    val len = randomAccessFile.read(chunk)
                    val http: HTTP =
                        HTTP.builder().baseUrl(getString(R.string.api))
                            .addMsgConvertor(GsonMsgConvertor()).build()
                    val res = http
                        .sync("upload")
                        .addHeader("token", MainApplication.getInstance().user?.token ?: "")
                        .addFilePara("file", "${file.name}_$i", chunk.copyOfRange(0, len))
                        .addBodyPara("num", chunkMd5.size.toString())
                        .addBodyPara("index", i.toString())
                        .addBodyPara("partMd5", chunkMd5[i])
                        .addBodyPara("fullPath", "")
                        .addBodyPara("nodeName", file.name)
                        .addBodyPara("size", file.length().toString())
                        .addBodyPara("fullMd5", fileMd5)
                        .setOnProcess {
                            if (Date().time - befTimeStamp >= 1000) { // 1秒调用一次
                                befTimeStamp = Date().time
                                MainApplication.getInstance()
                                    .uploadingUpdate(i.toLong() * CHUNK_SIZE + it.doneBytes)
                                onProgressChangeListener?.onProgressChange()
                            }
                        }
                        .nothrow()
                        .post()
                    if (!res.isSuccessful) {
                        Toast.makeText(this@UploadService, "上传失败", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
            MainApplication.getInstance()
                .uploadingUpdate(file.length())
            finishUpload(file)
        }

        /**
         * 完成上传，将记录插入数据库
         */
        private fun finishUpload(file: File) {
            MainApplication.getInstance().uploadingPop()
            MainApplication.getInstance().swapDataBase.swapNodeDao()
                .insertAll(SwapNode(0, true, Date(), file.length(), file.name))
            onCompleteTransferListener?.onCompleteTransfer()
        }

        /**
         * 获取已经上传分片序号数组
         */
        private fun getExistChunkArray(
            fileMd5: String,
            chunkLength: Int,
            existChunkArray: ArrayList<Int>
        ): Boolean {
            val r = OkHttpUtils.syncPost("checkAgain",
                Gson().toJson(
                    mapOf(
                        "fullMd5" to fileMd5,
                        "index" to Array(chunkLength) { 0 }
                    )
                )
            )
            if (!r.success) {
                Toast.makeText(this@UploadService, r.msg, Toast.LENGTH_SHORT).show()
                return false
            }
            existChunkArray.addAll(Gson().fromJson(r.msg, Array<Int>::class.java))
            return true
        }

        /**
         * 检查能否快速上传
         */
        private fun checkQuickUpload(file: File, fileMd5: String): Boolean {
            val r = OkHttpUtils.syncPost(
                "checkFile",
                Gson().toJson(QuickUploadRequest("", fileMd5, file.name, file.length()))
            )
            if (r.success) {
                val node = Gson().fromJson(r.msg, QuickUploadResponse::class.java)
                if (node.fileId == null || node.fileSize == -1) return false
                else {
                    MainApplication.getInstance().uploadingUpdate(file.length())
                    finishUpload(file)
                }
            } else {
                Toast.makeText(this@UploadService, r.msg, Toast.LENGTH_SHORT).show()
            }
            return true
        }
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "开始上传...", Toast.LENGTH_SHORT).show()
        serviceHandler?.obtainMessage()?.also { msg ->
            val bundle = Bundle()
            bundle.putString(
                "message",
                Gson().toJson(ServiceMessage(startId, intent.getStringExtra("path")!!))
            )
            msg.data = bundle
            serviceHandler?.sendMessage(msg)
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class UploadBinder : Binder() {
        fun getService(): UploadService = this@UploadService
    }

    override fun onDestroy() {
    }
}