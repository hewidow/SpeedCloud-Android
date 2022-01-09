package com.example.speedcloud.util

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
}