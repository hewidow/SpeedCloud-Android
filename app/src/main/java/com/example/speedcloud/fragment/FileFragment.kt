package com.example.speedcloud.fragment

import android.app.DownloadManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.SwapActivity
import com.example.speedcloud.adapter.RecyclerAdapter
import com.example.speedcloud.bean.Node
import com.example.speedcloud.listener.RecyclerListener
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
    private lateinit var adapter: RecyclerAdapter
    private lateinit var backArrowDrawable: Drawable
    private lateinit var fileOperationView: View
    private lateinit var fileOperationWindow: PopupWindow
    private lateinit var fileToolbarView: View
    private lateinit var fileToolbarWindow: PopupWindow
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var loading: ProgressBar
    private lateinit var selectNumber: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var cancel: Button
    private lateinit var selectAll: Button
    private lateinit var unselectAll: Button
    private lateinit var download: Button
    private lateinit var share: Button
    private lateinit var delete: Button
    private lateinit var rename: Button
    private lateinit var move: Button
    private var nodes: ArrayList<Node> = ArrayList() // 需要展示的文件
    private var backStack: ArrayList<Node> = ArrayList() // 文件目录返回栈
    private var selectedItem: ArrayList<Node> = ArrayList() // 选择的文件项下标

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
        initRefresh()
        initRecycler()

        return root
    }

    /**
     * 初始化刷新
     */
    private fun initRefresh() {
        loading = root.findViewById(R.id.loading)
        swipeRefresh = root.findViewById(R.id.swipeRefresh)

        // 设置刷新颜色
        swipeRefresh.setColorSchemeResources(
            R.color.blue_500, R.color.blue_700
        )
        // 设置下拉刷新监听器
        swipeRefresh.setOnRefreshListener {
            fetchChildren(backStack.last())
        }
    }

    /**
     * 初始化编辑模式的顶部操作栏
     */
    private fun initFileToolbar() {
        cancel = fileToolbarView.findViewById(R.id.cancel)
        selectAll = fileToolbarView.findViewById(R.id.selectAll)
        unselectAll = fileToolbarView.findViewById(R.id.unselectAll)
        selectNumber = fileToolbarView.findViewById(R.id.selectNumber)

        cancel.setOnClickListener { back() }
        selectAll.setOnClickListener {
            adapter.selectAllOrNot(true)
            selectAll.visibility = View.GONE
            unselectAll.visibility = View.VISIBLE
        }
        unselectAll.setOnClickListener {
            adapter.selectAllOrNot(false)
            unselectAll.visibility = View.GONE
            selectAll.visibility = View.VISIBLE
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
        download = fileOperationView.findViewById(R.id.download)
        share = fileOperationView.findViewById(R.id.share)
        delete = fileOperationView.findViewById(R.id.delete)
        rename = fileOperationView.findViewById(R.id.rename)
        move = fileOperationView.findViewById(R.id.move)
        download.setOnClickListener {
            getSelectedItem()
            val token = MainApplication.getInstance().user?.token
            val request =
                DownloadManager.Request(Uri.parse("${getString(R.string.baseUrl)}download?token=${token}&nodeId=${selectedItem[0].nodeId}&online=0"))
            request.setDestinationInExternalPublicDir(
                DIRECTORY_DOWNLOADS,
                "${selectedItem[0].nodeName}"
            )
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            request.setTitle("正在下载...")
            request.setDescription("SpeedCloud")
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            val id = MainApplication.getInstance().downloadManager.enqueue(request)
            Log.d("hgf", "download Id: ${id}")
        }
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
     * 获取选中的列表项
     */
    private fun getSelectedItem() {
        selectedItem.clear()
        for (i in adapter.checkStatus.indices) {
            if (adapter.checkStatus[i]) selectedItem.add(nodes[i])
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

    /**
     * 初始化工具栏
     */
    private fun initToolBar() {
        appbar = root.findViewById(R.id.appbar)
        toolbar = root.findViewById(R.id.toolbar)
        nestedScrollView = root.findViewById(R.id.nestedScrollView)

        appbar.addOnOffsetChangedListener(this) // 监听appbar收缩程度
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_swap -> {
                    startActivity(Intent(this.activity, SwapActivity::class.java))
                }
                R.id.menu_search -> {
                    nestedScrollView.fullScroll(View.FOCUS_UP) //主体向上滚动
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
            fileOperationWindow.showAsDropDown(root)
            fileToolbarWindow.showAtLocation(root, Gravity.START or Gravity.TOP, 0, 0)
            swipeRefresh.isEnabled = false
        }
    }

    /**
     * 实现点击勾选列表项的事件
     */
    inner class MyOnCheckedChangeListener : RecyclerListener.OnCheckedChangeListener {
        override fun onCheckedChange(view: View, position: Int, isChecked: Boolean) {
        }
    }

    /**
     * 实现选中列表项变化的事件
     */
    inner class MyOnSelectedItemNumberChangeListener :
        RecyclerListener.OnSelectedItemNumberChangeListener {
        override fun onSelectedItemNumberChange(value: Int) {
            download.isEnabled = (value == 1)
            share.isEnabled = (value > 0)
            delete.isEnabled = (value > 0)
            rename.isEnabled = (value == 1)
            move.isEnabled = (value == 1)
            selectNumber.text = "已选中${value}个文件"
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
        // 设置选中列表项数量变化监听器
        adapter.mOnSelectedItemNumberChangeListener = MyOnSelectedItemNumberChangeListener()
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
            if (!swipeRefresh.isRefreshing) {
                loading.visibility = View.VISIBLE
            } // 如果没有下拉刷新就显示loading
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
            swipeRefresh.isRefreshing = false
            loading.visibility = View.GONE // 移除loading
            adapter.setItems(nodes)
        }
    }

    /**
     * 返回能否回退上一层，能就直接回退上一层
     * @return true为可以回退，false为根目录，无法回退，即即将退出应用
     */
    fun back(): Boolean {
        if (adapter.selectStatus) {
            adapter.cancelSelect()
            swipeRefresh.isEnabled = true
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