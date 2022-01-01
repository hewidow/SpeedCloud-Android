package com.example.speedcloud.bean

data class Node(
    val createTime: String,
    val deleteTime: String?,
    val fileId: Int,
    val fileSize: Long,
    val isDirectory: Boolean,
    val nodeId: Int,
    val nodeName: String,
    val parentId: Int
)