package com.example.speedcloud.receiver

import android.app.DownloadManager.EXTRA_DOWNLOAD_ID
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.speedcloud.MainApplication
import com.example.speedcloud.util.DownloadManagerUtil

/**
 * DownloadManager广播接收器
 */
class CompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val task = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
        Log.d("hgf", "${DownloadManagerUtil.getDownloadProgress(task).third}")
        if (DownloadManagerUtil.getDownloadProgress(task).third == 0) { // 若是取消的，就删除记录
            MainApplication.getInstance().swapDataBase.swapNodeDao().deleteByTask(task)
        } else { // 完成就更新记录
            MainApplication.getInstance().swapDataBase.swapNodeDao().updateByTask(task)
        }
    }
}