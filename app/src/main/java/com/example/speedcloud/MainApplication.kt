package com.example.speedcloud

import android.app.Application
import androidx.room.Room
import com.example.speedcloud.bean.UploadingNode
import com.example.speedcloud.bean.User
import com.example.speedcloud.database.SwapDatabase

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
     * 更新任务进度
     */
    fun uploadingUpdate(startId: Int, value: Long) {
        for (node in uploadingNodes) {
            if (node.startId == startId) {
                node.uploaded = value
                break
            }
        }
    }

    /**
     * 去除完成的任务
     */
    fun uploadingFilter() {
        val temp: ArrayList<UploadingNode> = ArrayList()
        for (node in uploadingNodes) {
            if (node.uploaded < node.size) {
                temp.add(node)
            }
        }
        uploadingNodes = temp
    }
}