package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.R
import com.example.speedcloud.bean.Node
import com.example.speedcloud.util.FileUtil

class RecyclerAdapter(private var nodes: ArrayList<Node>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    // 根据布局绑定控件
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nodeName: TextView = view.findViewById(R.id.nodeName)
        val nodeInfo: TextView = view.findViewById(R.id.nodeInfo)
    }

    /**
     * 绑定每项的布局
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_row_file, viewGroup, false)
        return ViewHolder(view)
    }

    /**
     * 将每一项数据绑定到界面上
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.nodeName.text = nodes[position].nodeName
        var subTitle = "${nodes[position].createTime}"
        if (!nodes[position].isDirectory) subTitle += "  ${FileUtil.formatSize(nodes[position].fileSize)}"
        viewHolder.nodeInfo.text = subTitle
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