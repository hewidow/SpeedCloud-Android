package com.example.speedcloud.listener

import android.view.View

interface RecyclerListener {
    // 定义一个列表项的点击监听器接口
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    // 定义一个列表项的长按监听器接口
    interface OnItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }

    // 定义一个勾选列表项的点击监听器接口
    interface OnCheckedChangeListener {
        fun onCheckedChange(view: View, position: Int, isChecked: Boolean)
    }

    // 选中的列表项数目变化监听器
    interface OnSelectedItemNumberChangeListener {
        fun onSelectedItemNumberChange(value: Int)
    }

    // 进度条改变监听器
    interface OnProgressChangeListener {
        fun onProgressChange()
    }

    // 完成传输监听器
    interface OnCompleteTransferListener {
        fun onCompleteTransfer()
    }
}