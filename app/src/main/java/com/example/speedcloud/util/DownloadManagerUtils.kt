package com.example.speedcloud.util

import android.app.Application
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.bean.Node
import com.example.speedcloud.bean.SwapNode
import java.util.*

object DownloadManagerUtils {
    private val downloadManager = MainApplication.getInstance()
        .getSystemService(Application.DOWNLOAD_SERVICE) as DownloadManager

    /**
     * 根据id从DownloadManager中查询下载进度
     */
    fun getDownloadProgress(id: Long): Triple<Long, Long, Int> {
        val query = DownloadManager.Query().setFilterById(id)
        downloadManager.query(query)?.use { c ->
            if (c.moveToFirst()) {
                return Triple(
                    c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
                    c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                )
            }
        }
        return Triple(0, 0, 0)
    }

    /**
     * 发起下载请求
     */
    fun request(node: Node) {
        val token = MainApplication.getInstance().user?.token
        val request =
            DownloadManager.Request(
                Uri.parse(
                    "${
                        MainApplication.getInstance().getString(R.string.api)
                    }download?token=${token}&nodeId=${node.nodeId}&online=0"
                )
            )
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            node.nodeName
        )
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setTitle("正在下载 ${node.nodeName}")
        request.setDescription("SpeedCloud")
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        val id = downloadManager.enqueue(request)
        MainApplication.getInstance().swapDataBase.swapNodeDao()
            .insertAll(
                SwapNode(0, false, Date(), 0, node.nodeName, id, 0, 0, 0)
            ) // 往数据库中插入下载记录
    }

    /**
     * 移除下载任务
     */
    fun remove(task: Long) {
        downloadManager.remove(task)
    }
}