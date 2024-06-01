package com.example.speedcloud.converter

import androidx.room.TypeConverter
import java.util.*

// https://developer.android.google.cn/training/data-storage/room/referencing-data?hl=zh-cn
class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
