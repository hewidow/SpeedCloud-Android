package com.example.speedcloud.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.speedcloud.bean.SwapNode
import com.example.speedcloud.converter.DateConverters
import com.example.speedcloud.dao.SwapNodeDao

@Database(entities = [SwapNode::class], version = 1, exportSchema = false)
@TypeConverters(DateConverters::class)
abstract class SwapDatabase : RoomDatabase() {
    abstract fun swapNodeDao(): SwapNodeDao
}