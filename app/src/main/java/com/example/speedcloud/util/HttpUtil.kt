package com.example.speedcloud.util

import android.util.Log
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.bean.Result
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object HttpUtil {
    data class Response(
        val code: Int,
        val msg: String,
        val data: Any
    )

    private var baseUrl = MainApplication.getInstance().getString(R.string.baseUrl) // 根地址

    /**
     * 使用HttpURLConnection以Json的方式进行请求
     * @param requestMethod 请求方式
     * @param path 路径
     * @param data Json数据
     */
    private fun request(requestMethod: String, path: String, data: String): Result {
        val url = baseUrl + path
        Log.d("http:$requestMethod", "url:$url request:$data")
        var conn: HttpURLConnection? = null
        val bt = data.toByteArray()
        try {
            conn = URL(url).openConnection() as HttpURLConnection
            // 设置参数
            conn.requestMethod = requestMethod
            conn.doOutput = requestMethod == "POST"
            conn.doInput = true
            conn.useCaches = false
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("accept", "application/json")
            conn.setRequestProperty("token", MainApplication.getInstance().user?.token ?: "")
            conn.connectTimeout = 5000

            // 设置POST的请求头和请求体
            if (requestMethod == "POST") {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Content-Length", bt.size.toString())
                val out: OutputStream = conn.outputStream
                out.write(bt)
                out.flush()
                out.close()
            }

            // 响应体
            val inputStream: InputStream = if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream
            } else {
                conn.errorStream
            }
            val reader = BufferedReader(InputStreamReader(inputStream))
            var msg = reader.readText() // 读出二进制流中的信息并转为String
            reader.close()

            Log.d("http:$requestMethod", "url:$url code:${conn.responseCode} response:$msg")
            // 根据返回的格式抽离出data
            val temp = Gson().fromJson(msg, Response::class.java)
            msg = if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                Gson().toJson(temp.data)
            } else {
                temp.data.toString()
            }

            // 根据状态码返回响应结果
            if (conn.responseCode == 200) {
                return Result(true, msg)
            }
            return Result(false, msg)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }
        return Result(false, "连接超时")
    }

    fun get(path: String): Result {
        return request("GET", path, "")
    }

    fun post(path: String, data: String): Result {
        return request("POST", path, data)
    }
}