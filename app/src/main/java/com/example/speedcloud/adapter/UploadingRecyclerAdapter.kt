package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.bean.FileState
import com.example.speedcloud.bean.UploadingNode
import com.example.speedcloud.databinding.RowItemSwapLoadingBinding
import com.example.speedcloud.util.FileUtils

class UploadingRecyclerAdapter(private var nodes: ArrayList<UploadingNode>) :
    RecyclerView.Adapter<UploadingRecyclerAdapter.ViewHolder>() {
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
        holder.progressBar.progress =
            (nodes[position].uploaded * 100 / nodes[position].size).toInt()
        holder.nodeSize.text =
            "${FileUtils.formatSize(nodes[position].uploaded)}/${FileUtils.formatSize(nodes[position].size)}"
        holder.speed.text = when (nodes[position].state) {
            FileState.WAIT -> "正在等待"
            FileState.CALC -> "正在计算md5"
            else -> "${FileUtils.formatSize(nodes[position].speed)}/s"
        }
        // 以后再做
        holder.swapPause.visibility = View.GONE
        holder.swapPlay.visibility = View.GONE
        holder.swapCancel.visibility = View.GONE
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