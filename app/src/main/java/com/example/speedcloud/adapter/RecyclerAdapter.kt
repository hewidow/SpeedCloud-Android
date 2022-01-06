package com.example.speedcloud.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.R
import com.example.speedcloud.bean.Node
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.util.FileUtil

class RecyclerAdapter(private var nodes: ArrayList<Node>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    var onItemClickListener: RecyclerListener.OnItemClickListener? = null
    var onItemLongClickListener: RecyclerListener.OnItemLongClickListener? = null
    var onCheckedChangeListener: RecyclerListener.OnCheckedChangeListener? = null
    var onSelectedItemNumberChangeListener: RecyclerListener.OnSelectedItemNumberChangeListener? =
        null
    var selectStatus: Boolean = false
    var checkStatus: Array<Boolean> = Array(nodes.size) { false }
    private var selectedItemNumber: Int = 0
        set(value) {
            field = value
            onSelectedItemNumberChangeListener?.onSelectedItemNumberChange(value)
        }
    private lateinit var iconFolder: Drawable
    private var iconFolderColor: Int = 0
    private lateinit var iconFile: Drawable
    private var iconFileColor: Int = 0

    // 根据布局绑定控件
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nodeName: TextView = view.findViewById(R.id.nodeName)
        val nodeInfo: TextView = view.findViewById(R.id.nodeInfo)
        val rowItem: LinearLayout = view.findViewById(R.id.rowItem)
        val icon: ImageView = view.findViewById(R.id.icon)
        val checkBoxIn: View = view.findViewById(R.id.checkBoxIn)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }

    /**
     * 绑定每项的布局
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_item_file, viewGroup, false)
        iconFolder =
            ContextCompat.getDrawable(viewGroup.context, R.drawable.ic_baseline_folder_24)!!
        iconFolderColor = ContextCompat.getColor(viewGroup.context, R.color.icon_folder)
        iconFile = ContextCompat.getDrawable(
            viewGroup.context,
            R.drawable.ic_baseline_insert_drive_file_24
        )!!
        iconFileColor = ContextCompat.getColor(viewGroup.context, R.color.icon_file)
        return ViewHolder(view)
    }

    /**
     * 将每一项数据绑定到界面上
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // 为不同类型的文件设置相应的图标和颜色
        if (nodes[position].isDirectory) {
            viewHolder.icon.setImageDrawable(iconFolder) // 设置图标
            viewHolder.icon.setColorFilter(iconFolderColor) // 设置颜色
        } else {
            viewHolder.icon.setImageDrawable(iconFile)
            viewHolder.icon.setColorFilter(iconFileColor)
        }

        // 设置文件名字和附属信息
        viewHolder.nodeName.text = nodes[position].nodeName
        var subTitle = nodes[position].createTime
        if (!nodes[position].isDirectory) subTitle += "  ${FileUtil.formatSize(nodes[position].fileSize)}"
        viewHolder.nodeInfo.text = subTitle

        // 根据是否处于编辑模式绑定相应的监听事件
        if (selectStatus) {
            viewHolder.rowItem.setOnClickListener {
                viewHolder.checkBox.performClick()
            }
            viewHolder.rowItem.setOnLongClickListener(null)
            viewHolder.checkBoxIn.visibility = View.GONE
            viewHolder.checkBox.visibility = View.VISIBLE
        } else {
            // 设置item点击监听
            viewHolder.rowItem.setOnClickListener {
                onItemClickListener?.onItemClick(it, position) // 监听事件回调
            }
            // 设置item长按监听
            viewHolder.rowItem.setOnLongClickListener {
                checkStatus[position] = true // 长按的那一项设置为true
                selectedItemNumber = 1
                onItemLongClickListener?.onItemLongClick(it, position)
                startSelect()
                true
            }
            viewHolder.checkBoxIn.visibility = View.VISIBLE
            viewHolder.checkBox.visibility = View.GONE
        }

        // 设置checkBoxIn的勾选监听，效果和长按item项一样
        viewHolder.checkBoxIn.setOnClickListener {
            viewHolder.rowItem.performLongClick()
        }

        // 先设置为null，防止设置勾选时触发监听
        viewHolder.checkBox.setOnCheckedChangeListener(null)
        // 因为是recyclerView，所以需要根据保存的勾选状态设置checkBox
        viewHolder.checkBox.isChecked = checkStatus[position]
        viewHolder.rowItem.isSelected = checkStatus[position]

        // 设置checkBox勾选监听
        viewHolder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            checkStatus[position] = isChecked
            viewHolder.rowItem.isSelected = isChecked
            if (isChecked) selectedItemNumber += 1
            else selectedItemNumber -= 1
            onCheckedChangeListener?.onCheckedChange(buttonView, position, isChecked)
        }

    }

    override fun getItemCount() = nodes.size

    /**
     * 设置新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<Node>) {
        nodes = items
        checkStatus = Array(nodes.size) { false }
        selectedItemNumber = 0
        notifyDataSetChanged() // 通知数据刷新了
    }

    /**
     * 变为操作文件状态
     */
    @SuppressLint("NotifyDataSetChanged")
    fun startSelect() {
        selectStatus = true
        notifyDataSetChanged()
    }

    /**
     * 取消操作文件状态
     */
    @SuppressLint("NotifyDataSetChanged")
    fun cancelSelect() {
        checkStatus.fill(false) // 重置checkStatus状态
        selectedItemNumber = 0
        selectStatus = false
        notifyDataSetChanged()
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