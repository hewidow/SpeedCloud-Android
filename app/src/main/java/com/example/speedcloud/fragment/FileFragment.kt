package com.example.speedcloud.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.example.speedcloud.R
import com.example.speedcloud.SwapActivity
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

class FileFragment : Fragment(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var root: View
    private lateinit var appbar: AppBarLayout
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_file, container, false)
        appbar = root.findViewById(R.id.appbar)
        toolbar = root.findViewById(R.id.toolbar)

        appbar.addOnOffsetChangedListener(this) // 监听appbar收缩程度
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_swap -> {
                    startActivity(Intent(this.activity, SwapActivity::class.java))
                }
                R.id.menu_search -> {
                    root.findViewById<NestedScrollView>(R.id.nestedScrollView)
                        .fullScroll(View.FOCUS_UP) //主体向上滚动
                    appbar.setExpanded(true) //展开appbar
                } // 回到顶部
            }
            true
        }

        return root
    }

    // 折叠时显示搜索按钮，展开时隐藏搜索按钮
    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
            toolbar.menu.findItem(R.id.menu_search).isVisible = true
        } else if (verticalOffset == 0) {
            toolbar.menu.findItem(R.id.menu_search).isVisible = false
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            FileFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}