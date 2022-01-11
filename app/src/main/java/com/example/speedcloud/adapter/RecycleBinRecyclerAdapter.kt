package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.bean.Node
import com.example.speedcloud.databinding.RowItemRecycleBinBinding
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.util.DateUtils
import com.example.speedcloud.util.FileTypeUtils
import com.example.speedcloud.util.FileUtils
import java.util.*

class RecycleBinRecyclerAdapter(private var nodes: ArrayList<Node>) :
    RecyclerView.Adapter<RecycleBinRecyclerAdapter.ViewHolder>() {
    var onSelectedItemNumberChangeListener: RecyclerListener.OnSelectedItemNumberChangeListener? =
        null
    var checkStatus: Array<Boolean> = Array(nodes.size) { false }
    private var selectedItemNumber: Int = 0
        set(value) {
            field = value
            onSelectedItemNumberChangeListener?.onSelectedItemNumberChange(value)
        }

    inner class ViewHolder(binding: RowItemRecycleBinBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nodeName = binding.nodeName
        val nodeInfo = binding.nodeInfo
        val rowItem = binding.rowItem
        val icon = binding.icon
        val checkBox = binding.checkBox
        val remainTime = binding.remainTime
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(RowItemRecycleBinBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // 为不同类型的文件设置相应的图标和颜色
        val icon = FileTypeUtils.getIconDrawableAndColor(nodes[position].type)
        viewHolder.icon.setImageDrawable(icon.first) // 设置图标
        viewHolder.icon.setColorFilter(icon.second) // 设置颜色

        // 设置文件名字和附属信息
        viewHolder.nodeName.text = nodes[position].nodeName
        var subTitle = ""
        if (!nodes[position].isDirectory) subTitle += "${FileUtils.formatSize(nodes[position].fileSize)}"
        viewHolder.nodeInfo.text = subTitle
        viewHolder.remainTime.text =
            "${DateUtils.getRemainTimeText(nodes[position].deleteDate!!)}后清除"

        // 设置点击列表项回调
        viewHolder.rowItem.setOnClickListener {
            viewHolder.rowItem.isSelected = !viewHolder.rowItem.isSelected
            checkStatus[position] = viewHolder.rowItem.isSelected
            viewHolder.checkBox.isChecked = checkStatus[position]
            if (checkStatus[position]) ++selectedItemNumber
            else --selectedItemNumber
        }

        // 初始化选中状态
        viewHolder.checkBox.isChecked = checkStatus[position]
        viewHolder.rowItem.isSelected = checkStatus[position]
    }

    override fun getItemCount() = nodes.size

    /**
     * 整个列表项改变，刷新整个列表
     */
    @SuppressLint("NotifyDataSetChanged")
    fun changeAllItems() {
        checkStatus = Array(nodes.size) { false }
        selectedItemNumber = 0
        notifyDataSetChanged() // 通知数据刷新了
    }

    /**
     * 全选或全不选
     */
    @SuppressLint("NotifyDataSetChanged")
    fun selectAllOrNot(check: Boolean) {
        checkStatus.fill(check)
        selectedItemNumber = if (check) {
            nodes.size
        } else 0
        notifyDataSetChanged()
    }
}