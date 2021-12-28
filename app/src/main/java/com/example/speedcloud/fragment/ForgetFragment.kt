package com.example.speedcloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.speedcloud.R
import com.example.speedcloud.util.HttpUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ForgetFragment : Fragment() {
    private lateinit var root: View
    private lateinit var sendCode: Button
    private lateinit var forget: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_forget, container, false)
        sendCode = root.findViewById(R.id.sendCode)
        forget = root.findViewById(R.id.forget)
        sendCode.setOnClickListener {
            lifecycleScope.launch {
                sendCode.isEnabled = false
                val r = withContext(Dispatchers.IO) {
                    HttpUtil.post(
                        "/checkCode",
                        Gson().toJson(
                            mapOf(
                                "email" to root.findViewById<TextView>(R.id.email).text.toString(),
                                "type" to TYPE_FORGET // 0代表注册，1代表忘记密码
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
        forget.setOnClickListener {
            lifecycleScope.launch {
                forget.isEnabled = false
                val r = withContext(Dispatchers.IO) {
                    HttpUtil.post(
                        "/reset",
                        Gson().toJson(
                            mapOf(
                                "email" to root.findViewById<TextView>(R.id.email).text.toString(),
                                "checkCode" to root.findViewById<TextView>(R.id.code).text.toString(),
                                "password" to root.findViewById<TextView>(R.id.password).text.toString()
                            )
                        )
                    )
                }

                if (r.success) {
                    showMessage("重置密码成功")
                } else {
                    showMessage(r.msg)
                }
                forget.isEnabled = true
            }
        }
        return root
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this.activity, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TYPE_FORGET = 1 // 0代表注册，1代表忘记密码

        @JvmStatic
        fun newInstance() =
            ForgetFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}