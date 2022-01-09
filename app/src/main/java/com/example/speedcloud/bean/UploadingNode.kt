package com.example.speedcloud.bean

data class UploadingNode(
    val startId: Int,
    val name: String,
    val size: Long,
    var uploaded: Long
)