package com.example.speedcloud.bean

data class Result(
    /**
     * true或者false，true代表200请求，false代表除200外的错误请求
     */
    val success: Boolean,
    /**
     * 响应信息
     */
    val msg: String
)