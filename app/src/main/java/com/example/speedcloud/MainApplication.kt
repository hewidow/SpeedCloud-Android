package com.example.speedcloud

import android.app.Application
import android.util.Log
import com.example.speedcloud.bean.User

class MainApplication : Application() {
    companion object {
        private const val TAG: String = "MainApplication"
        private lateinit var mApp: MainApplication
        fun getInstance():MainApplication { return mApp }
    }
    var user: User? = null

    override fun onCreate() {
        super.onCreate()
        mApp = this
        Log.d(TAG, "onCreate")
    }
}