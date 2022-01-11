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
import com.example.speedcloud.RecycleBinActivity
import com.example.speedcloud.bean.User
import com.example.speedcloud.bean.UserDetail
import com.example.speedcloud.util.FileUtils
import com.example.speedcloud.util.HttpUtils
import com.example.speedcloud.util.SharedUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeFragment : Fragment() {

    private lateinit var root: View
    private lateinit var user: User
    private lateinit var logout: Button
    private lateinit var username: TextView
    private lateinit var pb_storage: ProgressBar
    private lateinit var tv_storage: TextView
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
        logout = root.findViewById(R.id.logout)
        logout.setOnClickListener {
            lifecycleScope.launch {
                SharedUtils.writeBoolean("autoLogin", false)
                val r = withContext(Dispatchers.IO) {
                    HttpUtils.get("/logout")
                }
                startAccountActivity()
            }
        }
        root.findViewById<TextView>(R.id.recycleBin).setOnClickListener {
            startRecycleBinActivity()
        }
        username = root.findViewById(R.id.username)
        pb_storage = root.findViewById(R.id.pb_storage)
        tv_storage = root.findViewById(R.id.tv_storage)
        loadData()
        return root
    }

    /**
     * 将数据装载进界面
     */
    private fun loadData() {
        user = MainApplication.getInstance().user!!
        username.text = user.userDetail.username
        pb_storage.progress =
            ((user.userDetail.totalSize - user.userDetail.availableSize) * 100F / user.userDetail.totalSize).toInt()
        tv_storage.text = getStorageText()
    }

    /**
     * 获取存储空间文本
     */
    private fun getStorageText(): String {
        return "${FileUtils.formatSize(user.userDetail.totalSize - user.userDetail.availableSize)} / ${
            FileUtils.formatSize(
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
                activity,
                AccountActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /**
     * 跳转到回收站界面
     */
    private fun startRecycleBinActivity() {
        startActivity(
            Intent(
                activity,
                RecycleBinActivity::class.java
            )
        )
    }

    /**
     * 切换到此fragment就查询用户的存储空间信息并更新到界面
     */
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) {
                HttpUtils.get("me")
            }
            if (r.success) {
                MainApplication.getInstance().user?.userDetail =
                    Gson().fromJson(r.msg, UserDetail::class.java)
                loadData()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}