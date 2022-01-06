package com.example.speedcloud

import android.app.Application
import android.app.DownloadManager
import androidx.room.Room
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
    lateinit var downloadManager: DownloadManager
    lateinit var swapDataBase: SwapDatabase

    override fun onCreate() {
        super.onCreate()
        mApp = this
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        swapDataBase =
            Room.databaseBuilder(
                applicationContext,
                SwapDatabase::class.java,
                "swap_database"
            )
                .allowMainThreadQueries()
                .build()
    }
}