package com.example.speedcloud.bean

data class UploadingNode(
    var state: FileState,
    val name: String,
    val size: Long,
    var uploaded: Long,
    var speed: Long
)