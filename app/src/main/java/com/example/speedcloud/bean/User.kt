package com.example.speedcloud.bean

data class User(
    val token: String,
    val login: Boolean,
    var userDetail: UserDetail
)