package com.example.speedcloud.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.R
import com.example.speedcloud.SwapActivity
import com.example.speedcloud.adapter.RecyclerAdapter
import com.example.speedcloud.bean.Node
import com.example.speedcloud.util.HttpUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class FileFragment : Fragment(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var root: View
    private lateinit var appbar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var recycler: RecyclerView
    private lateinit var nodes: ArrayList<Node>
    private lateinit var adapter: RecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        nodes = ArrayList()
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
        } // 设置toolbar上的按钮监听
        initRecycler()
        return root
    }

    /**
     * 初始化文件列表
     */
    private fun initRecycler() {
        recycler = root.findViewById(R.id.recycler)
        // 设置一个垂直方向的网格布局管理器
        recycler.layoutManager = GridLayoutManager(this.activity, 1)
        // 设置数据适配器
        adapter = RecyclerAdapter(nodes)
        recycler.adapter = adapter
        fetchChildren(1)
    }

    /**
     * 获取某个文件夹下的所有子文件
     */
    private fun fetchChildren(nodeId: Int) {
        lifecycleScope.launch {
            nodes.clear()
            adapter.setItems(nodes) // 清空之前的文件
            root.findViewById<ProgressBar>(R.id.loading).visibility = View.VISIBLE // 显示loading
            val r = withContext(Dispatchers.IO) {
                HttpUtil.get("queryChildren?nodeId=${nodeId}")
            }
            if (r.success) {
                val gson =
                    GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create() // 设置接收的时间格式
                nodes.addAll(
                    gson.fromJson(
                        r.msg,
                        Array<Node>::class.java
                    ) // 转为文件数组
                )
            } else {
                showMessage(r.msg)
            }
            root.findViewById<ProgressBar>(R.id.loading).visibility = View.GONE // 移除loading
            adapter.setItems(nodes) // 设置文件
        }
    }

    /**
     * 折叠时显示搜索按钮，展开时隐藏搜索按钮
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
            toolbar.menu.findItem(R.id.menu_search).isVisible = true
        } else if (verticalOffset == 0) {
            toolbar.menu.findItem(R.id.menu_search).isVisible = false
        }
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this.activity, msg, Toast.LENGTH_SHORT).show()
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