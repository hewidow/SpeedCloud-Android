package com.example.speedcloud.bean

class DownloadingNode(
    val name: String, // 名字
    val task: Long, // 编号
    var size: Long, // 大小
    var progress: Long,// 进度
    var state: Int, // 状态
    var speed: Long, // 速度
)