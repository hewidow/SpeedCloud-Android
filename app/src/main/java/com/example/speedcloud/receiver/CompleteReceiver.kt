package com.example.speedcloud.receiver

import android.app.DownloadManager.EXTRA_DOWNLOAD_ID
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.speedcloud.MainApplication

class CompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val task = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
        MainApplication.getInstance().swapDataBase.swapNodeDao().updateByTask(task)
    }
}