package com.example.speedcloud.bean

data class UserDetail(
    val userId: Int,
    val username: String,
    val email: String,
    val totalSize: Long,
    val availableSize: Long,
    val banned: Boolean,
    val roleName: String
)