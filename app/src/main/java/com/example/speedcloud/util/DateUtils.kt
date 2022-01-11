package com.example.speedcloud.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    /**
     * 获取剩余时间文本
     */
    fun getRemainTimeText(time: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dif = Date().time - dateFormat.parse(time).time + 1000 * 60 * 60 * 24 * 10
        return "${dif / (1000 * 60 * 60 * 24)}天${dif % (1000 * 60 * 60 * 24) / (1000 * 60 * 60)}时${dif % (1000 * 60 * 60) / (1000 * 60)}分"
    }
}