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
import com.example.speedcloud.util.HttpUtil
import com.example.speedcloud.util.SharedUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeFragment : Fragment() {

    private lateinit var root: View
    private var user = MainApplication.getInstance().user!!
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
        root.findViewById<TextView>(R.id.tv_name).text = user.userDetail.username
        root.findViewById<ProgressBar>(R.id.pb_storage).progress =
            ((user.userDetail.totalSize - user.userDetail.availableSize) * 100F / user.userDetail.totalSize).toInt()
        root.findViewById<TextView>(R.id.tv_storage).text = getStorageText()
        root.findViewById<Button>(R.id.logout).setOnClickListener {
            lifecycleScope.launch {
                SharedUtil.writeBoolean("autoLogin", false)
                val r = withContext(Dispatchers.IO) {
                    HttpUtil.get(
                        "/logout",
                        ""
                    )
                }
                startAccountActivity()
            }
        }
        return root
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
        startActivity(Intent(this.activity, AccountActivity::class.java))
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