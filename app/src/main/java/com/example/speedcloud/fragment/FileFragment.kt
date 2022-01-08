package com.example.speedcloud.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import com.example.speedcloud.bean.ShareLink
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.util.DownloadManagerUtil
import com.example.speedcloud.util.HttpUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class FileFragment : Fragment() {


    private lateinit var root: View
    private lateinit var appbar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecyclerAdapter
    private lateinit var backArrowDrawable: Drawable
    private lateinit var fileToolbarView: View
    private lateinit var fileToolbarWindow: PopupWindow
    private lateinit var fileActionbarView: View
    private lateinit var fileActionbarWindow: PopupWindow
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
        // 获取编辑模式的底部工具栏view对象
        fileToolbarView = inflater.inflate(R.layout.popup_window_file_toolbar, container, false)
        initFileToolbar()

        // 获取编辑模式的顶部操作栏view对象
        fileActionbarView = inflater.inflate(R.layout.popup_window_file_actionbar, container, false)
        initFileActionbar()

        root = inflater.inflate(R.layout.fragment_file, container, false)
        initAppbar()
        initRefresh()
        initRecycler()

        return root
    }

    /**
     * 初始化下拉刷新和进入文件夹的加载
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
            refreshPath()
        }
    }

    /**
     * 初始化编辑模式的顶部操作栏
     */
    private fun initFileActionbar() {
        cancel = fileActionbarView.findViewById(R.id.cancel)
        selectAll = fileActionbarView.findViewById(R.id.selectAll)
        unselectAll = fileActionbarView.findViewById(R.id.unselectAll)
        selectNumber = fileActionbarView.findViewById(R.id.selectNumber)

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
        fileActionbarWindow = PopupWindow(
            fileActionbarView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        fileActionbarWindow.animationStyle = R.style.popup_window_top_bottom_anim
    }

    /**
     * 设置背景透明度
     */
    private fun setBackgroundAlpha(alpha: Float) {
        val lp = activity!!.window.attributes
        lp.alpha = alpha
        activity!!.window.attributes = lp
    }

    /**
     * 初始化编辑模式的底部工具栏
     */
    private fun initFileToolbar() {
        download = fileToolbarView.findViewById(R.id.download)
        share = fileToolbarView.findViewById(R.id.share)
        delete = fileToolbarView.findViewById(R.id.delete)
        rename = fileToolbarView.findViewById(R.id.rename)
        move = fileToolbarView.findViewById(R.id.move)
        download.setOnClickListener {
            getSelectedItem()
            showDialog("确认下载", "将使用移动数据或WIFI进行下载") {
                DownloadManagerUtil.request(selectedItem[0])
                Toast.makeText(context, "开始下载...", Toast.LENGTH_SHORT).show()
            }
        }
        share.setOnClickListener {
            getSelectedItem()
            val view = layoutInflater.inflate(R.layout.dialog_share, null)
            val dialog = AlertDialog.Builder(context).setView(view).create()
            val spinner = view.findViewById<Spinner>(R.id.spinner)
            ArrayAdapter.createFromResource(
                context!!,
                R.array.share_time_array,
                R.layout.item_spinner
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.item_spinner)
                spinner.adapter = adapter
            } // 设置适配器
            spinner.setSelection(1) // 设置”三天“那一项
            view.findViewById<TextView>(R.id.copyLink).setOnClickListener {
                dialog.dismiss()
                back()
                lifecycleScope.launch {
                    val r = withContext(Dispatchers.IO) {
                        HttpUtil.post(
                            "share", Gson().toJson(
                                mapOf(
                                    "srcNodeIds" to selectedItem.map { it.nodeId },
                                    "type" to 1
                                )
                            )
                        )
                    } // 获取分享链接
                    if (r.success) {
                        val shareLink = Gson().fromJson(r.msg, ShareLink::class.java)
                        (MainApplication.getInstance()
                            .getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                            ClipData.newPlainText(
                                "SpeedCloud Share Link",
                                "分享链接:\n${getString(R.string.host)}share?id=${shareLink.uniqueId}\n提取码: ${shareLink.checkCode}"
                            )
                        )
                        Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } // 设置点击复制链接监听
            dialog.show()
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.window!!.setWindowAnimations(R.style.popup_window_bottom_top_anim)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // 去除默认的16dp的padding，设置背景使其宽度和屏幕一致
            dialog.window!!.decorView.setPadding(0, 0, 0, 0)
            dialog.window!!.decorView.setBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.background_secondary
                )
            )
        }
        delete.setOnClickListener {
            getSelectedItem()
            showDialog("删除文件", "10天内可在回收站中找回已删文件") {
                lifecycleScope.launch {
                    val r = withContext(Dispatchers.IO) {
                        HttpUtil.post("deleteNode", Gson().toJson(selectedItem.map { it.nodeId }))
                    }
                    Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
                    refreshPath()
                }
            }
        }
        rename.setOnClickListener {
            getSelectedItem()
            val view = layoutInflater.inflate(R.layout.dialog_rename, null)
            val name = view.findViewById<EditText>(R.id.name)
            val dialog = AlertDialog.Builder(context).setTitle("重命名").setView(view)
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                    back()
                    lifecycleScope.launch {
                        val r = withContext(Dispatchers.IO) {
                            HttpUtil.post(
                                "renameNode", Gson().toJson(
                                    mapOf(
                                        "newName" to name.text.toString(),
                                        "nodeId" to selectedItem[0].nodeId
                                    )
                                )
                            )
                        }
                        if (r.success) {
                            refreshPath()
                            Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }.create()
            dialog.show()
            name.setText(selectedItem[0].nodeName)
            name.requestFocus() // 请求焦点
//            (MainApplication.getInstance()
//                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
//                name,
//                InputMethodManager.SHOW_FORCED
//            ) // 打开键盘
        }
        // 设置窗口大小
        fileToolbarWindow = PopupWindow(
            fileToolbarView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        // 设置动画效果
        fileToolbarWindow.animationStyle = R.style.popup_window_bottom_top_anim
    }

    /**
     * 生成对话框
     */
    private fun showDialog(title: String, message: String, onClickListener: View.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.dialog_alert, null)
        val dialog = builder.setCancelable(false).setView(view).create()
        view.findViewById<TextView>(R.id.title).text = title
        view.findViewById<TextView>(R.id.message).text = message
        view.findViewById<TextView>(R.id.cancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<TextView>(R.id.confirm).setOnClickListener {
            dialog.dismiss()
            back()
            onClickListener.onClick(it)
        }
        dialog.show()
        val displayRectangle = Rect()
        activity!!.window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        dialog.window!!.setLayout(
            (displayRectangle.width() * 0.75).toInt(),
            dialog.window!!.attributes.height
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

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
     * 初始化程序应用栏
     */
    private fun initAppbar() {
        appbar = root.findViewById(R.id.appbar)
        toolbar = root.findViewById(R.id.toolbar)
        nestedScrollView = root.findViewById(R.id.nestedScrollView)

        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                toolbar.menu.findItem(R.id.menu_search).isVisible = true
            } else if (verticalOffset == 0) {
                toolbar.menu.findItem(R.id.menu_search).isVisible = false
            }
        }) // 监听appbar收缩程度，折叠时显示搜索按钮，展开时隐藏搜索按钮
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
                refreshPath(arrayListOf(nodes[position]))
            } else {
            }
        }
    }

    /**
     * 实现长按列表项的事件
     */
    inner class MyOnItemLongClickListener : RecyclerListener.OnItemLongClickListener {
        override fun onItemLongClick(view: View, position: Int) {
            fileToolbarWindow.showAsDropDown(root)
            fileActionbarWindow.showAtLocation(root, Gravity.START or Gravity.TOP, 0, 0)
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
        adapter.onItemClickListener = MyOnItemClickListener()
        // 设置长按监听器
        adapter.onItemLongClickListener = MyOnItemLongClickListener()
        // 设置勾选监听器
        adapter.onCheckedChangeListener = MyOnCheckedChangeListener()
        // 设置选中列表项数量变化监听器
        adapter.onSelectedItemNumberChangeListener = MyOnSelectedItemNumberChangeListener()
        // 给recycler设置适配器
        recycler.adapter = adapter
        refreshPath()
    }

    /**
     * 按照所给的数组进入相应的路径
     * @param path 若为空或不填，则刷新当前目录；若第一个元素nodeId为1，则为绝对路径，替换当前backStack，否则为相对路径
     */
    private fun refreshPath(path: ArrayList<Node> = ArrayList()) {
        if (path.isNotEmpty() && path[0].nodeId == 1) backStack = path
        else backStack.addAll(path)
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
     * 返回能否回退上一层，能就直接回退上一层。注意：如需调用getSelectedItem()，需在调用back()前
     * @return true为可以回退，false为已经到达根目录，无法回退，即将退出应用
     */
    fun back(): Boolean {
        if (adapter.selectStatus) {
            adapter.cancelSelect()
            swipeRefresh.isEnabled = true
            fileToolbarWindow.dismiss()
            fileActionbarWindow.dismiss()
            return true
        } // 处于select模式
        if (backStack.size <= 1) return false
        backStack.removeLast()
        refreshPath()
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