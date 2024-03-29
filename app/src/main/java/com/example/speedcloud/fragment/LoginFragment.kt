package com.example.speedcloud.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.speedcloud.MainActivity
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.bean.Result
import com.example.speedcloud.bean.User
import com.example.speedcloud.util.HttpUtils
import com.example.speedcloud.util.SharedUtils
import com.google.gson.Gson
import kotlinx.coroutines.*

class LoginFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var root: View
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_login, container, false)
        button = root.findViewById(R.id.login)

        // 绑定登录事件
        button.setOnClickListener { clickButton() }

        // 获取共享参数中的信息
        val username = SharedUtils.readString("username", "")
        val password = SharedUtils.readString("password", "")
        val check = SharedUtils.readBoolean("autoLogin", false)

        root.findViewById<EditText>(R.id.username).setText(username)
        root.findViewById<EditText>(R.id.password).setText(password)
        root.findViewById<CheckBox>(R.id.autoLogin).isChecked = check

        // 勾选自动登录且用户名和密码不为空，就自动登录
        if (check && !username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            clickButton()
        }

        return root
    }

    /**
     * 底部弹出消息
     */
    private fun showMessage(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 点击登录按钮事件
     */
    private fun clickButton() {
        // 创建协程在主线程
        lifecycleScope.launch {
            // 请求开始，禁用按钮
            button.isEnabled = false
            // 将用户名和密码存入共享内存
            SharedUtils.writeString(
                "username",
                root.findViewById<EditText>(R.id.username).text.toString()
            )
            SharedUtils.writeString(
                "password",
                root.findViewById<EditText>(R.id.password).text.toString()
            )
            SharedUtils.writeBoolean(
                "autoLogin",
                root.findViewById<CheckBox>(R.id.autoLogin).isChecked
            )
            // 网络请求在IO线程完成
            val r = withContext(Dispatchers.IO) {
                login(
                    root.findViewById<EditText>(R.id.username).text.toString(),
                    root.findViewById<EditText>(R.id.password).text.toString()
                )
            }

            // 根据返回结果处理信息
            if (r.success) {
                MainApplication.getInstance().user = Gson().fromJson(r.msg, User::class.java)
                startMainActivity()
            } else {
                showMessage(r.msg)
            }
            // 请求完成，启用按钮
            button.isEnabled = true
        }
    }

    /**
     * 登录接口
     * @param username 用户名
     * @param password 密码
     * @return 用户信息和token
     */
    private fun login(username: String, password: String): Result {
        return HttpUtils.post(
            "login",
            Gson().toJson(
                mapOf(
                    "username" to username,
                    "password" to password
                )
            )
        )
    }

    /**
     * 跳转到主界面
     */
    private fun startMainActivity() {
        startActivity(
            Intent(
                activity,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    companion object {
        private const val TAG: String = "Login"

        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}