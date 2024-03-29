package com.example.speedcloud.bean

import java.util.*


/**
 * 文件节点
 */
data class Node(
    val createTime: String,
    val deleteTime: String?,
    val fileId: Int,
    val fileSize: Long,
    val isDirectory: Boolean,
    val nodeId: Int,
    val nodeName: String,
    val parentId: Int,
    var type: FileType = FileType.DIRECTORY,
    var deleteDate: Date? = null
)
