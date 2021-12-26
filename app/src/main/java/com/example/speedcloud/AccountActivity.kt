package com.example.speedcloud

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.speedcloud.databinding.ActivityAccountBinding
import com.example.speedcloud.fragment.ForgetFragment
import com.example.speedcloud.fragment.LoginFragment
import com.example.speedcloud.fragment.RegisterFragment

class AccountActivity : AppCompatActivity() {
    companion object {
        enum class FragmentType {
            LOGIN, REGISTER, FORGET
        } // 定义Fragment的枚举类
    }
    private lateinit var binding: ActivityAccountBinding
    private lateinit var iFragment: Array<Fragment?> // Fragment实例
    private lateinit var cFragment: Array<()->Fragment> //newInstance的函数引用
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root) // 使用ViewBinding

        // 绑定fragment跳转
        binding.tvLogin.setOnClickListener { setFragment(FragmentType.LOGIN) }
        binding.tvRegister.setOnClickListener { setFragment(FragmentType.REGISTER) }
        binding.tvForget.setOnClickListener { setFragment(FragmentType.FORGET) }

        iFragment = Array(FragmentType.values().size) { null } // 初始化为null
        cFragment = arrayOf(LoginFragment::newInstance, RegisterFragment::newInstance, ForgetFragment::newInstance)
        setFragment(FragmentType.LOGIN)
    }

    /**
     * 切换登录、注册、忘记密码的fragment
     */
    private fun setFragment(type: FragmentType) {
        setTextButton(type)
        val mFragmentManager = supportFragmentManager
        val mTransaction = mFragmentManager.beginTransaction()
        for (i in iFragment.indices) {
            iFragment[i]?.let {
                mTransaction.hide(it)
            } // 不为null才隐藏
        }
        if (iFragment[type.ordinal] == null) { // 未创建实例，则创建并添加
            iFragment[type.ordinal] = cFragment[type.ordinal]()
            mTransaction.add(R.id.fl_main, iFragment[type.ordinal]!!)
        } else { // 已创建则显示
            mTransaction.show(iFragment[type.ordinal]!!)
        }
        mTransaction.commitAllowingStateLoss()
    }

    /**
     * 设置对应文本按钮跳转
     */
    private fun setTextButton(type: FragmentType) {
        binding.tvLogin.visibility = View.VISIBLE
        binding.tvRegister.visibility = View.VISIBLE
        binding.tvForget.visibility = View.VISIBLE
        when(type) {
            FragmentType.LOGIN -> binding.tvLogin.visibility = View.GONE
            FragmentType.REGISTER -> binding.tvRegister.visibility = View.GONE
            else -> binding.tvForget.visibility = View.GONE
        }
    }
}