package com.example.speedcloud.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.speedcloud.AccountActivity
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.bean.User
import com.example.speedcloud.bean.UserDetail
import com.example.speedcloud.util.HttpUtil
import com.example.speedcloud.util.SharedUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeFragment : Fragment() {

    private lateinit var root: View
    private lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_me, container, false)
        root.findViewById<Button>(R.id.logout).setOnClickListener {
            lifecycleScope.launch {
                SharedUtil.writeBoolean("autoLogin", false)
                val r = withContext(Dispatchers.IO) {
                    HttpUtil.get("/logout")
                }
                startAccountActivity()
            }
        }
        initView()
        return root
    }

    /**
     * 将数据装载进界面
     */
    private fun initView() {
        user = MainApplication.getInstance().user!!
        root.findViewById<TextView>(R.id.username).text = user.userDetail.username
        root.findViewById<ProgressBar>(R.id.pb_storage).progress =
            ((user.userDetail.totalSize - user.userDetail.availableSize) * 100F / user.userDetail.totalSize).toInt()
        root.findViewById<TextView>(R.id.tv_storage).text = getStorageText()
    }

    /**
     * 将字节大小转换为文本
     */
    private fun formatSize(size: Long): String {
        var s = size
        for (i in 0..sizeText.size - 2) {
            if (s < 1024) return "$s${sizeText[i]}"
            s /= 1024
        }
        return "$s${sizeText[sizeText.size - 1]}"
    }

    /**
     * 获取存储空间文本
     */
    private fun getStorageText(): String {
        return "${formatSize(user.userDetail.totalSize - user.userDetail.availableSize)} / ${
            formatSize(
                user.userDetail.totalSize
            )
        }"
    }

    /**
     * 跳转到登录界面
     */
    private fun startAccountActivity() {
        startActivity(
            Intent(
                this.activity,
                AccountActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /**
     * 切换到此fragment就查询用户的存储空间信息并更新到界面
     */
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) {
                HttpUtil.get("me")
            }
            if (r.success) {
                MainApplication.getInstance().user?.userDetail =
                    Gson().fromJson(r.msg, UserDetail::class.java)
                initView()
            }
        }
    }

    companion object {
        val sizeText = arrayOf("B", "KB", "MB", "GB")

        @JvmStatic
        fun newInstance() =
            MeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}