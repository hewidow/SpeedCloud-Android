package com.example.speedcloud.util

import com.example.speedcloud.bean.FileType
import com.example.speedcloud.bean.Node
import java.text.DecimalFormat

object FileUtils {
    private val sizeText = arrayOf("B", "KB", "MB", "GB")
    private val df = DecimalFormat("0.00")

    /**
     * 将字节大小转换为文本
     */
    fun formatSize(size: Long): String {
        var s: Double = size.toDouble()
        for (i in 0..sizeText.size - 2) {
            if (s < 1024) return "${df.format(s)}${sizeText[i]}"
            s /= 1024
        }
        return "${df.format(s)}${sizeText[sizeText.size - 1]}"
    }

    /**
     * 格式化文件数据
     */
    fun formatData(nodes: ArrayList<Node>) {
        for (node in nodes) {
            node.type = when {
                node.isDirectory -> FileType.DIRECTORY
                node.nodeName.contains(Regex("\\.png|\\.jpg|\\.jpeg|\\.gif")) -> FileType.IMAGE
                node.nodeName.contains(".mp4") -> FileType.VIDEO
                else -> FileType.DEFAULT
            }
            node.deleteTime?.let { node.deleteDate = DateUtils.timeToDate(it) }
        }
        nodes.sortByDescending { -it.type.ordinal }
    }

    /**
     * 按删除日期由近到远排序
     */
    fun sortDataByDeleteDate(nodes: ArrayList<Node>) {
        nodes.sortByDescending { it.deleteDate?.time ?: 0 }
    }

    /**
     * 根据类型筛选数据，需先调用formatData()获取type
     */
    fun filterDataByType(nodes: ArrayList<Node>, type: FileType) {
        (nodes.filter { it.type == type } as ArrayList<Node>).also {
            nodes.clear()
            nodes.addAll(it)
        }
    }

    /**
     * 根据名字筛选数据
     */
    fun filterDataByName(nodes: ArrayList<Node>, contain: String) {
        (nodes.filter { it.nodeName.contains(contain) } as ArrayList<Node>).also {
            nodes.clear()
            nodes.addAll(it)
        }
    }
}