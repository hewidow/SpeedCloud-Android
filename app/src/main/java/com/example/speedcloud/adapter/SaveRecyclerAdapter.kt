package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.bean.Node
import com.example.speedcloud.databinding.RowItemSaveBinding
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.util.FileTypeUtils
import com.example.speedcloud.util.FileUtils

class SaveRecyclerAdapter(private var nodes: ArrayList<Node>) :
    RecyclerView.Adapter<SaveRecyclerAdapter.ViewHolder>() {

    var onItemClickListener: RecyclerListener.OnItemClickListener? = null

    inner class ViewHolder(binding: RowItemSaveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nodeName: TextView = binding.nodeName
        val nodeInfo: TextView = binding.nodeInfo
        val rowItem: LinearLayout = binding.rowItem
        val icon: ImageView = binding.icon
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // 使用viewBinding绑定
        return ViewHolder(RowItemSaveBinding.inflate(LayoutInflater.from(viewGroup.context)))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // 为不同类型的文件设置相应的图标和颜色
        val icon = FileTypeUtils.getIconDrawableAndColor(nodes[position].type)
        viewHolder.icon.setImageDrawable(icon.first) // 设置图标
        viewHolder.icon.setColorFilter(icon.second) // 设置颜色

        // 设置文件名字和附属信息
        viewHolder.nodeName.text = nodes[position].nodeName
        var subTitle = nodes[position].createTime
        if (!nodes[position].isDirectory) subTitle += "  ${FileUtils.formatSize(nodes[position].fileSize)}"
        viewHolder.nodeInfo.text = subTitle

        // 设置点击列表项回调
        viewHolder.rowItem.setOnClickListener {
            onItemClickListener?.onItemClick(it, position)
        }
    }

    override fun getItemCount() = nodes.size

    /**
     * 设置新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<Node>) {
        nodes = items
        notifyDataSetChanged() // 通知数据刷新了
    }
}