package com.example.speedcloud.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    /**
     * 获取剩余时间文本
     */
    fun getRemainTimeText(date: Date): String {
        val dif = date.time - Date().time + 1000 * 60 * 60 * 24 * 10
        return "${dif / (1000 * 60 * 60 * 24)}天${dif % (1000 * 60 * 60 * 24) / (1000 * 60 * 60)}时${dif % (1000 * 60 * 60) / (1000 * 60)}分"
    }

    /**
     * 时间文本转时间
     */
    fun timeToDate(time: String): Date? {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(time)
    }
}