package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.bean.UploadingNode
import com.example.speedcloud.databinding.RowItemSwapLoadingBinding
import com.example.speedcloud.util.FileUtils

class UploadingRecyclerAdapter(private var nodes: ArrayList<UploadingNode>) :
    RecyclerView.Adapter<UploadingRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(binding: RowItemSwapLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nodeName: TextView = binding.nodeName
        val nodeSize: TextView = binding.nodeSize
        val speed: TextView = binding.speed
        val progressBar: ProgressBar = binding.progressBar
        val swapPause: ImageButton = binding.swapPause
        val swapPlay: ImageButton = binding.swapPlay
        val swapCancel: ImageButton = binding.swapCancel
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
        // 以后再做
        holder.speed.text = ""
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