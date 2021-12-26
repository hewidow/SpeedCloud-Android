package com.example.speedcloud

import android.app.Application
import android.util.Log

class MainApplication : Application() {
    companion object {
        private const val TAG: String = "MainApplication"
        private lateinit var mApp: MainApplication
        fun getInstance():MainApplication { return mApp }
    }

    override fun onCreate() {
        super.onCreate()
        mApp = this
        Log.d(TAG, "onCreate")
    }
}