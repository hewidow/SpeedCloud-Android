package com.example.speedcloud.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class SwapNode(
    @PrimaryKey(autoGenerate = true) val id: Int, // 全局自增id
    @ColumnInfo(name = "type") val type: Boolean, // false->下载，true->上传
    @ColumnInfo(name = "time") val time: Date?, // 记录创建时间
    @ColumnInfo(name = "size") var size: Long, // 文件大小
    @ColumnInfo(name = "name") val name: String, // 文件名字
    @ColumnInfo(name = "task") val task: Long,// 下载编号，0表示不在下载
    @ColumnInfo(name = "progress") var progress: Long,// 下载进度
    @ColumnInfo(name = "state") var state: Int, // 下载状态
    @ColumnInfo(name = "speed") var speed: Long, // 下载速度
)