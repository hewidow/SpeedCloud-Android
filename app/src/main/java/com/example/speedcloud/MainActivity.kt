package com.example.speedcloud

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.speedcloud.databinding.ActivityMainBinding
import com.example.speedcloud.fragment.FileFragment
import com.example.speedcloud.fragment.MeFragment
import com.example.speedcloud.util.DialogUtils
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mContext: Context
    private lateinit var fileFragment: FileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        fileFragment = FileFragment.newInstance()

        // 设置ViewPage2的适配器
        binding.viewPage.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> fileFragment
                    else -> MeFragment.newInstance()
                }
            }
        }

        // 禁止左右滑动
        binding.viewPage.isUserInputEnabled = false

        // 使用TabLayoutMediator将TabLayout和ViewPage2进行双向绑定
        TabLayoutMediator(binding.tabLayout, binding.viewPage) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "文件"
                    tab.icon = ContextCompat.getDrawable(mContext, R.drawable.tab_selector_file)
                }
                else -> {
                    tab.text = "我的"
                    tab.icon = ContextCompat.getDrawable(mContext, R.drawable.tab_selector_me)
                }
            }
        }.attach()

        // 动态请求存储权限
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ), 1
        )
    }

    /**
     * 重写回退方法
     */
    override fun onBackPressed() {
        // 不在文件页面或已经退到根目录
        if (binding.tabLayout.selectedTabPosition != 0 || !fileFragment.back()) super.onBackPressed()
    }

    /**
     * 监听剪贴板跳转分享文件页面
     */
    override fun onResume() {
        super.onResume()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.text
        text?.also { r ->
            val shareIdRes = Regex("http://101\\.43\\.111\\.132/share\\?id=(\\d+)").find(r)
            val uniqueId: String? = shareIdRes?.groupValues?.get(1)
            val codeRes = Regex("提取码：(\\d{4})").find(r)
            val code: String? = codeRes?.groupValues?.get(1)
            uniqueId?.also {
                clipboard.setPrimaryClip(ClipData.newPlainText("SpeedCloud Share Link", ""))
                DialogUtils.showShareDialog(this, code) {
                    startActivity(
                        Intent(this, ShareActivity::class.java)
                            .putExtra("uniqueId", uniqueId)
                            .putExtra("code", code)
                    )
                }
            }
        }
    }
}