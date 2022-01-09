package com.example.speedcloud.util

import android.util.Log
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.bean.Result
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK

object OkHttpUtils {
    private var api = MainApplication.getInstance().getString(R.string.api) // 根地址

    /**
     * 同步post，提交json
     */
    fun syncPost(path: String, requestBody: String): Result {
        return syncPost(path, requestBody.toRequestBody("application/json".toMediaType()))
    }

    /**
     * 同步post
     */
    fun syncPost(path: String, requestBody: RequestBody): Result {
        val url = api + path
        val request = Request.Builder()
            .url(url)
            .addHeader("token", MainApplication.getInstance().user?.token ?: "")
            .addHeader("Accept", "application/json")
            .post(requestBody)
            .build()
        try {
            val response: Response = OkHttpClient().newCall(request).execute()
            val temp = Gson().fromJson(
                response.body!!.string(),
                HttpUtils.Response::class.java
            )
            Log.d("okHttp", "url:$url code:${response.code} response:$temp")
            return if (response.code == HTTP_OK) Result(true, Gson().toJson(temp.data))
            else Result(false, temp.data.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Result(false, "连接超时")
    }
}