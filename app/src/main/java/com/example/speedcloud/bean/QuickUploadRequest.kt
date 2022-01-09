package com.example.speedcloud.bean

data class QuickUploadRequest(
    val fullPath: String,
    val md5: String,
    val nodeName: String,
    val size: Long
)
