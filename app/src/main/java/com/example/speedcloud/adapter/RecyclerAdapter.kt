package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.R
import com.example.speedcloud.bean.Node
import com.example.speedcloud.interfaces.RecyclerListener
import com.example.speedcloud.util.FileUtil

class RecyclerAdapter(private var nodes: ArrayList<Node>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    var mOnItemClickListener: RecyclerListener.OnItemClickListener? = null
    var mOnItemLongClickListener: RecyclerListener.OnItemLongClickListener? = null

    // 根据布局绑定控件
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nodeName: TextView = view.findViewById(R.id.nodeName)
        val nodeInfo: TextView = view.findViewById(R.id.nodeInfo)
        val rowItem: LinearLayout = view.findViewById(R.id.rowItem)
        val icon: ImageView = view.findViewById(R.id.icon)
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
        if (nodes[position].isDirectory) {
            viewHolder.icon.setImageDrawable(
                ContextCompat.getDrawable(
                    viewHolder.itemView.context,
                    R.drawable.ic_baseline_folder_24
                )
            ) // 设置图标
            viewHolder.icon.setColorFilter(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.icon_folder
                )
            ) // 设置颜色
        } else {
            viewHolder.icon.setImageDrawable(
                ContextCompat.getDrawable(
                    viewHolder.itemView.context,
                    R.drawable.ic_baseline_insert_drive_file_24
                )
            )
            viewHolder.icon.setColorFilter(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.icon_file
                )
            )
        } // 为不同类型的文件设置相应的图标和颜色
        viewHolder.nodeName.text = nodes[position].nodeName
        var subTitle = nodes[position].createTime
        if (!nodes[position].isDirectory) subTitle += "  ${FileUtil.formatSize(nodes[position].fileSize)}"
        viewHolder.nodeInfo.text = subTitle
        viewHolder.rowItem.setOnClickListener {
            mOnItemClickListener?.onItemClick(it, position)
        }
        viewHolder.rowItem.setOnLongClickListener {
            mOnItemLongClickListener?.onItemLongClick(it, position)
            true
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