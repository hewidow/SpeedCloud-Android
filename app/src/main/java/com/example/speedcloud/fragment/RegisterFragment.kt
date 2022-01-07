package com.example.speedcloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.speedcloud.R
import com.example.speedcloud.util.HttpUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegisterFragment : Fragment() {
    private lateinit var root: View
    private lateinit var sendCode: Button
    private lateinit var register: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_register, container, false)
        sendCode = root.findViewById(R.id.sendCode)
        register = root.findViewById(R.id.register)
        sendCode.setOnClickListener {
            lifecycleScope.launch {
                sendCode.isEnabled = false
                val r = withContext(Dispatchers.IO) {
                    HttpUtil.post(
                        "checkCode",
                        Gson().toJson(
                            mapOf(
                                "email" to root.findViewById<EditText>(R.id.email).text.toString(),
                                "type" to TYPE_REGISTER // 0代表注册，1代表忘记密码
                            )
                        )
                    )
                }
                if (r.success) {
                    showMessage("发送成功")
                } else {
                    showMessage(r.msg)
                }
                sendCode.isEnabled = true
            }
        }
        register.setOnClickListener {
            lifecycleScope.launch {
                register.isEnabled = false
                val r = withContext(Dispatchers.IO) {
                    HttpUtil.post(
                        "register",
                        Gson().toJson(
                            mapOf(
                                "email" to root.findViewById<EditText>(R.id.email).text.toString(),
                                "checkCode" to root.findViewById<EditText>(R.id.code).text.toString(),
                                "password" to root.findViewById<EditText>(R.id.password).text.toString(),
                                "username" to root.findViewById<EditText>(R.id.username).text.toString()
                            )
                        )
                    )
                }
                if (r.success) {
                    showMessage("注册成功")
                } else {
                    showMessage(r.msg)
                }
                register.isEnabled = true
            }
        }
        return root
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this.activity, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TYPE_REGISTER = 0 // 0代表注册，1代表忘记密码

        @JvmStatic
        fun newInstance() =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}