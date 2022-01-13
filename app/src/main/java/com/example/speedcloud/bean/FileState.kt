package com.example.speedcloud.bean

enum class FileState {
    WAIT, CALC, LOADING
    // WAIT->正在等待，CALC->正在计算md5，LOADING->正在上传
}