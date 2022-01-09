package com.example.speedcloud.util

import com.example.speedcloud.bean.FileType
import com.example.speedcloud.bean.Node
import java.text.DecimalFormat

object FileUtil {
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
        }
    }
}