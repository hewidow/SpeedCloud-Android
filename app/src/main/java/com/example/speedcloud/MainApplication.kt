package com.example.speedcloud

import android.app.Application
import androidx.room.Room
import com.example.speedcloud.bean.*
import com.example.speedcloud.database.SwapDatabase
import java.util.*
import kotlin.collections.ArrayList

class MainApplication : Application() {
    companion object {
        private const val TAG: String = "MainApplication"
        private lateinit var mApp: MainApplication
        fun getInstance(): MainApplication {
            return mApp
        }
    }

    var user: User? = null
    lateinit var swapDataBase: SwapDatabase
    var uploadingNodes: ArrayList<UploadingNode> = ArrayList()
    var downloadingNodes: ArrayList<DownloadingNode> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        mApp = this
        swapDataBase =
            Room.databaseBuilder(
                applicationContext,
                SwapDatabase::class.java,
                "swap_database"
            )
                .allowMainThreadQueries()
                .build()
    }

    /**
     * 改变状态
     */
    fun uploadingState(state: FileState) {
        uploadingNodes[0].state = state
    }

    /**
     * 添加一个新的上传任务
     */
    fun uploadingPush(uploadingNode: UploadingNode) {
        uploadingNodes.add(uploadingNode)
    }

    /**
     * 更新上传任务进度
     */
    fun uploadingUpdate(value: Long) {
        uploadingNodes[0].speed = value - uploadingNodes[0].uploaded
        uploadingNodes[0].uploaded = value
    }

    /**
     * 去除完成的任务
     */
    fun uploadingPop() {
        uploadingNodes.removeFirst()
    }

    /**
     * 下载结束
     * @param task 任务id
     * @param cancel 是否是用户主动取消的
     */
    fun downloadingDone(task: Long, cancel: Boolean) {
        for (i in downloadingNodes.indices) {
            if (downloadingNodes[i].task == task) {
                if (!cancel) swapDataBase.swapNodeDao()
                    .insertAll(
                        SwapNode(
                            0,
                            false,
                            Date(),
                            downloadingNodes[i].size,
                            downloadingNodes[i].name
                        )
                    )
                downloadingNodes.removeAt(i)
                break
            }
        }
    }
}