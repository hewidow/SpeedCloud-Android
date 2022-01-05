package com.example.speedcloud.bean

data class DownloadNode(
    val time: String,
    val id: Int,
    val size: Long,
    val name: String,
    val progress: Long
)