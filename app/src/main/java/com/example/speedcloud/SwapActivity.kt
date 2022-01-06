package com.example.speedcloud

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.speedcloud.databinding.ActivitySwapBinding
import com.example.speedcloud.fragment.DownloadFragment
import com.example.speedcloud.fragment.UploadFragment
import com.google.android.material.tabs.TabLayoutMediator

class SwapActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySwapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 设置ViewPage2的适配器
        binding.viewPage.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> DownloadFragment.newInstance()
                    else -> UploadFragment.newInstance()
                }
            }
        }

        // 使用TabLayoutMediator将TabLayout和ViewPage2进行双向绑定
        TabLayoutMediator(binding.tabLayout, binding.viewPage) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "下载"
                }
                else -> {
                    tab.text = "上传"
                }
            }
        }.attach()
    }
}