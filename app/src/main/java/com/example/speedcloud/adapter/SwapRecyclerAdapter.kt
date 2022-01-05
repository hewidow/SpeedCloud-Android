package com.example.speedcloud.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.bean.DownloadNode
import com.example.speedcloud.databinding.ItemSwapBinding

class SwapRecyclerAdapter(private var nodes: ArrayList<DownloadNode>) :
    RecyclerView.Adapter<SwapRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(binding: ItemSwapBinding) : RecyclerView.ViewHolder(binding.root) {
        val nodeName: TextView = binding.nodeName
        val nodeInfo: TextView = binding.nodeInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用viewBinding绑定
        return ViewHolder(ItemSwapBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}