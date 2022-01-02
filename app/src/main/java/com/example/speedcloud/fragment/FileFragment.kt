package com.example.speedcloud.fragment

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.speedcloud.R
import com.example.speedcloud.SwapActivity
import com.example.speedcloud.adapter.RecyclerAdapter
import com.example.speedcloud.bean.Node
import com.example.speedcloud.interfaces.RecyclerListener
import com.example.speedcloud.util.HttpUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
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
    private lateinit var backStack: ArrayList<Node>
    private lateinit var backArrowDrawable: Drawable
    private var selectStatus: Boolean = false// 是否处于选择文件模式
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        backArrowDrawable =
            ContextCompat.getDrawable(this.context!!, R.drawable.ic_baseline_arrow_back_ios_24)!!
        nodes = ArrayList()
        backStack = ArrayList()
        // 加入开始的根目录id
        backStack.add(Node("", "", 0, 0, true, 1, "全部文件", 0))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_file, container, false)
        initToolBar()
        initRecycler()
        return root
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

    /**
     * 初始化工具栏
     */
    private fun initToolBar() {
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

        toolbar.title = "全部文件"
        toolbar.setNavigationOnClickListener { back() }
        toolbar.navigationIcon = null
    }

    inner class MyOnItemClickListener : RecyclerListener.OnItemClickListener {
        override fun onItemClick(view: View, position: Int) {
            if (nodes[position].isDirectory) { // 是文件夹
                backStack.add(nodes[position]) // 加入back栈
                fetchChildren(nodes[position]) // 获取新目录
            } else {
            }
        }
    }

    inner class MyOnItemLongClickListener : RecyclerListener.OnItemLongClickListener {
        override fun onItemLongClick(view: View, position: Int) {
            selectStatus = true
            adapter.startSelect()
        }
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
        // 设置点击监听器
        adapter.mOnItemClickListener = MyOnItemClickListener()
        // 设置长按监听器
        adapter.mOnItemLongClickListener = MyOnItemLongClickListener()
        // 给recycler设置适配器
        recycler.adapter = adapter
        fetchChildren(backStack.last())
    }

    /**
     * 获取某个文件夹下的所有子文件
     */
    private fun fetchChildren(node: Node) {
        lifecycleScope.launch {
            toolbar.title = node.nodeName // 设置标题名字
            if (node.nodeId == 1) {
                toolbar.navigationIcon = null
            } else {
                toolbar.navigationIcon = backArrowDrawable
            } // 看是否是根目录设置是否有返回键
            nodes.clear() // 清空之前的文件
            adapter.setItems(nodes) // 设置数据
            root.findViewById<ProgressBar>(R.id.loading).visibility = View.VISIBLE // 显示loading
            val r = withContext(Dispatchers.IO) {
                HttpUtil.get("queryChildren?nodeId=${node.nodeId}")
            }
            if (r.success) {
                nodes.addAll(
                    Gson().fromJson(
                        r.msg,
                        Array<Node>::class.java
                    ) // 转为文件数组
                )
            } else {
                showMessage(r.msg)
            }
            nodes.sortByDescending { it.isDirectory }
            root.findViewById<ProgressBar>(R.id.loading).visibility = View.GONE // 移除loading
            adapter.setItems(nodes)
        }
    }

    /**
     * 返回能否回退上一层，能就直接回退上一层
     * @return true为可以回退，false为根目录，无法回退，即即将退出应用
     */
    fun back(): Boolean {
        if (selectStatus) {
            selectStatus = false
            adapter.cancelSelect()
            return true
        } // 处于select模式
        if (backStack.size <= 1) return false
        backStack.removeLast()
        fetchChildren(backStack.last())
        return true
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