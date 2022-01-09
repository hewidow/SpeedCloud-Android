package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.bean.SwapNode
import com.example.speedcloud.databinding.RowItemSwapBinding
import com.example.speedcloud.util.FileUtils
import java.text.SimpleDateFormat

class SwapLoadedRecyclerAdapter(private var nodes: ArrayList<SwapNode>) :
    RecyclerView.Adapter<SwapLoadedRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(binding: RowItemSwapBinding) : RecyclerView.ViewHolder(binding.root) {
        val nodeName: TextView = binding.nodeName
        val nodeInfo: TextView = binding.nodeInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用viewBinding绑定
        return ViewHolder(RowItemSwapBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nodeName.text = nodes[position].name
        holder.nodeInfo.text =
            "${FileUtils.formatSize(nodes[position].size)}  ${
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                    nodes[position].time
                )
            }"
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