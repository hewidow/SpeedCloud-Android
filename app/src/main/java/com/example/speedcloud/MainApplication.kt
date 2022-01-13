package com.example.speedcloud

import android.app.Application
import androidx.room.Room
import com.example.speedcloud.bean.FileState
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
}