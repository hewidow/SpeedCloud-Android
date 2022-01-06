package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.MainApplication
import com.example.speedcloud.bean.SwapNode
import com.example.speedcloud.databinding.RowItemSwapLoadingBinding
import com.example.speedcloud.util.FileUtil

class SwapLoadingRecyclerAdapter(private var nodes: ArrayList<SwapNode>) :
    RecyclerView.Adapter<SwapLoadingRecyclerAdapter.ViewHolder>() {
    private val swapDatabase = MainApplication.getInstance().swapDataBase

    inner class ViewHolder(binding: RowItemSwapLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nodeName: TextView = binding.nodeName
        val nodeSize: TextView = binding.nodeSize
        val speed: TextView = binding.speed
        val progressBar: ProgressBar = binding.progressBar
        val button: LinearLayout = binding.button
        val swapCancel: ImageButton = binding.swapCancel
        val swapPause: ImageButton = binding.swapPause
        val swapPlay: ImageButton = binding.swapPlay
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用viewBinding绑定
        return ViewHolder(RowItemSwapLoadingBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nodeName.text = nodes[position].name
        if (nodes[position].state == DownloadManager.STATUS_RUNNING || nodes[position].state == DownloadManager.STATUS_PAUSED) {
            holder.nodeSize.text =
                "${FileUtil.formatSize(nodes[position].progress)}/${FileUtil.formatSize(nodes[position].size)}"
            holder.speed.text = "${FileUtil.formatSize(nodes[position].speed)}/s"
            holder.progressBar.progress =
                (nodes[position].progress * 100 / nodes[position].size).toInt()
        } else {
            holder.nodeSize.text =
                "${FileUtil.formatSize(nodes[position].size)}"
            holder.speed.text = "正在等待"
            holder.progressBar.progress = 0
        }
        holder.button.visibility = View.GONE
    }

    override fun getItemCount() = nodes.size

    /**
     * 设置新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<SwapNode>) {
        nodes = items
        notifyDataSetChanged() // 通知数据刷新了
    }
}