package com.example.speedcloud.util

object FileUtil {
    private val sizeText = arrayOf("B", "KB", "MB", "GB")

    /**
     * 将字节大小转换为文本
     */
    fun formatSize(size: Long): String {
        var s = size
        for (i in 0..sizeText.size - 2) {
            if (s < 1024) return "$s${sizeText[i]}"
            s /= 1024
        }
        return "$s${sizeText[sizeText.size - 1]}"
    }
}