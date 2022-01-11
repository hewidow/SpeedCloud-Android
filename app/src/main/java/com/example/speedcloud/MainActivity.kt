package com.example.speedcloud

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.speedcloud.databinding.ActivityMainBinding
import com.example.speedcloud.fragment.FileFragment
import com.example.speedcloud.fragment.MeFragment
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

    override fun onBackPressed() {
        // 不在文件页面或已经退到根目录
        if (binding.tabLayout.selectedTabPosition != 0 || !fileFragment.back()) super.onBackPressed()
    }
}