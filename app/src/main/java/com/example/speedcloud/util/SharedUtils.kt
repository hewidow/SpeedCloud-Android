package com.example.speedcloud.util

import android.content.Context
import android.content.SharedPreferences
import com.example.speedcloud.MainApplication

object SharedUtils {
    // 声明一个共享参数的实例
    private var mShared: SharedPreferences =
        MainApplication.getInstance().getSharedPreferences("share", Context.MODE_PRIVATE)

    /**
     * 把键名与字符串的配对信息写入共享参数
     */
    fun writeString(key: String, value: String) {
        val editor = mShared.edit() // 获得编辑器的对象
        editor.putString(key, value) // 添加一个指定键名的字符串参数
        editor.apply() // 提交编辑器中的修改
    }

    /**
     * 根据键名到共享参数中查找对应的字符串对象
     * @param key 键名
     * @param defaultValue 键名找不到时返回
     */
    fun readString(key: String, defaultValue: String): String? {
        return mShared.getString(key, defaultValue)
    }

    /**
     * 把键名与整型数的配对信息写入共享参数
     */
    fun writeInt(key: String, value: Int) {
        val editor = mShared.edit()
        editor.putInt(key, value) // 添加一个指定键名的整型数参数
        editor.apply()
    }

    /**
     * 根据键名到共享参数中查找对应的整型数对象
     */
    fun readInt(key: String, defaultValue: Int): Int {
        return mShared.getInt(key, defaultValue)
    }

    /**
     * 把键名与布尔型的配对信息写入共享参数
     */
    fun writeBoolean(key: String, value: Boolean) {
        val editor = mShared.edit()
        editor.putBoolean(key, value) // 添加一个指定键名的整型数参数
        editor.apply()
    }

    /**
     * 根据键名到共享参数中查找对应的布尔型对象
     */
    fun readBoolean(key: String, defaultValue: Boolean): Boolean {
        return mShared.getBoolean(key, defaultValue)
    }
}