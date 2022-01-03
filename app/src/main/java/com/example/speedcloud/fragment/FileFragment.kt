package com.example.speedcloud.fragment

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
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
    private var nodes: ArrayList<Node> = ArrayList() // 需要展示的文件
    private lateinit var adapter: RecyclerAdapter
    private var backStack: ArrayList<Node> = ArrayList() // 文件目录返回栈
    private lateinit var backArrowDrawable: Drawable
    private var selectStatus: Boolean = false // 是否处于编辑模式
    private lateinit var fileOperationView: View
    private lateinit var fileOperationWindow: PopupWindow
    private lateinit var fileToolbarView: View
    private lateinit var fileToolbarWindow: PopupWindow
    private var selectedItem: ArrayList<Node> = ArrayList() // 选择的文件项
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        backArrowDrawable =
            ContextCompat.getDrawable(this.context!!, R.drawable.ic_baseline_arrow_back_ios_24)!!
        // 加入开始的根目录id
        backStack.add(Node("", "", 0, 0, true, 1, "全部文件", 0))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 获取编辑模式的底部操作栏view对象
        fileOperationView = inflater.inflate(R.layout.window_file_operation, container, false)
        initFileOperator()

        // 获取编辑模式的顶部操操作栏view对象
        fileToolbarView = inflater.inflate(R.layout.window_file_toolbar, container, false)
        initFileToolbar()

        root = inflater.inflate(R.layout.fragment_file, container, false)
        initToolBar()
        initRecycler()
        return root
    }

    /**
     * 初始化编辑模式的顶部操作栏
     */
    private fun initFileToolbar() {
        fileToolbarView.findViewById<Button>(R.id.cancel).setOnClickListener { back() }
        fileToolbarView.findViewById<Button>(R.id.selectAll).setOnClickListener {
            adapter.selectAllOrNot(true)
            fileToolbarView.findViewById<Button>(R.id.selectAll).visibility = View.GONE
            fileToolbarView.findViewById<Button>(R.id.unselectAll).visibility = View.VISIBLE
        }
        fileToolbarView.findViewById<Button>(R.id.unselectAll).setOnClickListener {
            adapter.selectAllOrNot(false)
            fileToolbarView.findViewById<Button>(R.id.unselectAll).visibility = View.GONE
            fileToolbarView.findViewById<Button>(R.id.selectAll).visibility = View.VISIBLE
        }
        fileToolbarWindow = PopupWindow(
            fileToolbarView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        fileToolbarWindow.animationStyle = R.style.popup_window_top_bottom_anim
    }

    /**
     * 初始化编辑模式的底部操作栏
     */
    private fun initFileOperator() {
        // 设置窗口大小
        fileOperationWindow = PopupWindow(
            fileOperationView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        // 设置动画效果
        fileOperationWindow.animationStyle = R.style.popup_window_bottom_top_anim
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

    /**
     * 实现点击列表项的事件
     */
    inner class MyOnItemClickListener : RecyclerListener.OnItemClickListener {
        override fun onItemClick(view: View, position: Int) {
            if (nodes[position].isDirectory) { // 是文件夹
                backStack.add(nodes[position]) // 加入back栈
                fetchChildren(nodes[position]) // 获取新目录
            } else {
            }
        }
    }

    /**
     * 实现长按列表项的事件
     */
    inner class MyOnItemLongClickListener : RecyclerListener.OnItemLongClickListener {
        override fun onItemLongClick(view: View, position: Int) {
            selectStatus = true
            fileOperationWindow.showAsDropDown(root)
            fileToolbarWindow.showAtLocation(root, Gravity.START or Gravity.TOP, 0, 0)
            adapter.startSelect()
        }
    }

    /**
     * 实现点击勾选列表项的事件
     * 注意：
     * 此处为方便起见，直接通过adapter获取已选择的列表项，当notifyDataSetChanged时若有多个check改变状态改变会触发多次这个事件！！！
     */
    inner class MyOnCheckedChangeListener : RecyclerListener.OnCheckedChangeListener {
        override fun onCheckedChange(view: View, position: Int, isChecked: Boolean) {
            selectedItem = adapter.getSelectedItem()
            fileOperationView.findViewById<Button>(R.id.download).isEnabled =
                (selectedItem.size == 1)
            fileOperationView.findViewById<Button>(R.id.share).isEnabled = (selectedItem.size > 0)
            fileOperationView.findViewById<Button>(R.id.delete).isEnabled = (selectedItem.size > 0)
            fileOperationView.findViewById<Button>(R.id.rename).isEnabled = (selectedItem.size == 1)
            fileOperationView.findViewById<Button>(R.id.move).isEnabled = (selectedItem.size == 1)
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
        // 设置勾选监听器
        adapter.mOnCheckedChangeListener = MyOnCheckedChangeListener()
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
            fileOperationWindow.dismiss()
            fileToolbarWindow.dismiss()
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