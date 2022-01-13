package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.bean.DownloadingNode
import com.example.speedcloud.databinding.RowItemSwapLoadingBinding
import com.example.speedcloud.util.DownloadManagerUtils
import com.example.speedcloud.util.FileUtils

class DownloadingRecyclerAdapter(private var nodes: ArrayList<DownloadingNode>) :
    RecyclerView.Adapter<DownloadingRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(binding: RowItemSwapLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nodeName = binding.nodeName
        val nodeSize = binding.nodeSize
        val speed = binding.speed
        val progressBar = binding.progressBar
        val swapPause = binding.swapPause
        val swapPlay = binding.swapPlay
        val swapCancel = binding.swapCancel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用viewBinding绑定
        return ViewHolder(RowItemSwapLoadingBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nodeName.text = nodes[position].name
        if (nodes[position].state == DownloadManager.STATUS_RUNNING || nodes[position].state == DownloadManager.STATUS_PAUSED) {
            holder.nodeSize.text =
                "${FileUtils.formatSize(nodes[position].progress)}/${FileUtils.formatSize(nodes[position].size)}"
            holder.speed.text = "${FileUtils.formatSize(nodes[position].speed)}/s"
            holder.progressBar.progress =
                (nodes[position].progress * 100 / nodes[position].size).toInt()
        } else {
            holder.nodeSize.text =
                "${FileUtils.formatSize(nodes[position].size)}"
            holder.speed.text = "正在等待"
            holder.progressBar.progress = 0
        }
        // DownloadManager不支持暂停
        holder.swapPause.visibility = View.GONE
        holder.swapPlay.visibility = View.GONE

        // 取消下载任务
        holder.swapCancel.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context).setTitle("确定取消")
                .setPositiveButton("确定") { _, _ ->
                    DownloadManagerUtils.remove(nodes[position].task)
                }.setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }.create().show()
        }
    }

    override fun getItemCount() = nodes.size

    /**
     * 整个列表项改变，刷新整个列表
     */
    @SuppressLint("NotifyDataSetChanged")
    fun changeAllItems() {
        notifyDataSetChanged() // 通知数据刷新了
    }
}