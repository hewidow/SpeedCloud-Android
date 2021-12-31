package com.example.speedcloud.bean

import java.util.*

data class Node(
    val createTime: Date,
    val deleteTime: Date?,
    val fileId: Int,
    val fileSize: Long,
    val isDirectory: Boolean,
    val nodeId: Int,
    val nodeName: String,
    val parentId: Int
)
