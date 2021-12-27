package com.example.speedcloud

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this

        // 设置ViewPage2的适配器
        binding.vpPage.adapter = object: FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when(position) {
                    0->FileFragment.newInstance()
                    else->MeFragment.newInstance()
                }
            }
        }

        // 使用TabLayoutMediator将TabLayout和ViewPage2进行双向绑定
        TabLayoutMediator(binding.tlTab, binding.vpPage) { tab, position ->
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
    }
}