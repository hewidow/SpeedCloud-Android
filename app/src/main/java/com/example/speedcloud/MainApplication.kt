package com.example.speedcloud

import android.app.Application
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

    override fun onCreate() {
        super.onCreate()
        mApp = this
    }
}