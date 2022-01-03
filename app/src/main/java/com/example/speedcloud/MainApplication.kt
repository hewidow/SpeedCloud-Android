package com.example.speedcloud

import android.app.Application
import android.app.DownloadManager
import com.example.speedcloud.bean.User

class MainApplication : Application() {
    companion object {
        private const val TAG: String = "MainApplication"
        private lateinit var mApp: MainApplication
        fun getInstance(): MainApplication {
            return mApp
        }
    }

    var user: User? = null
    lateinit var downloadManager: DownloadManager


    override fun onCreate() {
        super.onCreate()
        mApp = this
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }
}