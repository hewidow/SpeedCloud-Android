package com.example.speedcloud.fragment

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.speedcloud.*
import com.example.speedcloud.adapter.RecyclerAdapter
import com.example.speedcloud.bean.FileType
import com.example.speedcloud.bean.Node
import com.example.speedcloud.bean.ShareLink
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.service.UploadService
import com.example.speedcloud.util.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var search: EditText
    private lateinit var floatingActionButton: FloatingActionButton
    private var originalNodes: ArrayList<Node> = ArrayList() // 原始的文件数组
    private var nodes: ArrayList<Node> = ArrayList() // 需要展示的文件
    private var backStack: ArrayList<Node> = ArrayList() // 文件目录返回栈
    private var selectedItem: ArrayList<Node> = ArrayList() // 选择的文件项下标

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        backArrowDrawable =
            ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_arrow_back_ios_24)!!
        // 加入开始的根目录id
        backStack.add(Node("", "", 0, 0, true, 1, "全部文件", 0, FileType.DIRECTORY))
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
        initSearch()
        initRefresh()
        initRecycler()
        initFloatingActionButton()
        return root
    }

    /**
     * 初始化搜索
     */
    private fun initSearch() {
        search = root.findViewById(R.id.search)
        search.addTextChangedListener {
            // 不在loading时才触发，可以避免获取子目录时触发此事件
            if (!swipeRefresh.isRefreshing && loading.visibility != View.VISIBLE) {
                nodes.clear()
                nodes.addAll(originalNodes)
                FileUtils.filterDataByName(nodes, search.text.toString())
                adapter.changeAllItems()
            }
        }
    }

    /**
     * 初始化下拉刷新和进入文件夹的加载
     */
    private fun initRefresh() {
        loading = root.findViewById(R.id.loading)
        swipeRefresh = root.findViewById(R.id.swipeRefresh)

        // 设置刷新颜色
        swipeRefresh.setColorSchemeResources(
            R.color.primary, R.color.primary_variant
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
            DialogUtils.showAlertDialog(context!!, "确认下载", "将使用移动数据或WIFI进行下载") {
                back()
                DownloadManagerUtils.request(selectedItem[0])
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
                        HttpUtils.post(
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
            DialogUtils.showAlertDialog(context!!, "删除文件", "10天内可在回收站中找回已删文件") {
                back()
                lifecycleScope.launch {
                    val r = withContext(Dispatchers.IO) {
                        HttpUtils.post("deleteNode", Gson().toJson(selectedItem.map { it.nodeId }))
                    }
                    if (r.success) {
                        refreshPath()
                    } else {
                        Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        rename.setOnClickListener {
            getSelectedItem()
            val view = layoutInflater.inflate(R.layout.dialog_edit_text, null)
            val name = view.findViewById<EditText>(R.id.editText)
            val dialog = AlertDialog.Builder(context).setTitle("重命名").setView(view)
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                    back()
                    lifecycleScope.launch {
                        val r = withContext(Dispatchers.IO) {
                            HttpUtils.post(
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
                        } else {
                            Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }.create()
            dialog.show()
            dialog.getButton(Dialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(context!!, R.color.text_secondary))
            name.setText(selectedItem[0].nodeName)
            name.requestFocus() // 请求焦点
//            (MainApplication.getInstance()
//                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
//                name,
//                InputMethodManager.SHOW_FORCED
//            ) // 打开键盘
        }
        move.setOnClickListener {
            getSelectedItem()
            val fragmentManager = activity!!.supportFragmentManager
            val newFragment = SaveDialogFragment { id ->
                back()
                lifecycleScope.launch {
                    val r = withContext(Dispatchers.IO) {
                        HttpUtils.post(
                            "moveNode", Gson().toJson(
                                mapOf(
                                    "dstNodeId" to id,
                                    "srcNodeId" to selectedItem.map { it.nodeId }
                                )
                            )
                        )
                    }
                    if (r.success) refreshPath()
                    else Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
                }
            } // 确认移动后的回调
            newFragment.show(fragmentManager, "moveDialog")
        } // https://developer.android.google.cn/guide/topics/ui/dialogs?hl=zh-cn#DismissingADialog
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
     * 初始化新建文件夹和上传入口
     */
    private fun initFloatingActionButton() {
        floatingActionButton = root.findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_add, null)
            val dialog = BottomSheetDialog(context!!, R.style.BottomSheetDialog)
            dialog.setContentView(view)
            view.findViewById<TextView>(R.id.newFolder).setOnClickListener {
                dialog.dismiss()
                DialogUtils.showCreateFolderDialog(context!!) { name ->
                    lifecycleScope.launch {
                        val r = withContext(Dispatchers.IO) {
                            HttpUtils.post(
                                "createNode", Gson().toJson(
                                    mapOf(
                                        "nodeName" to name,
                                        "parentId" to backStack.last().nodeId
                                    )
                                )
                            )
                        }
                        if (r.success) {
                            refreshPath()
                        } else {
                            Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                } // 新建文件夹对话框的回调函数
            } // 新建文件夹
            view.findViewById<TextView>(R.id.upload).setOnClickListener {
                dialog.dismiss()
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, REQUEST_CHOOSE_FILE)
            } // 打开系统自带的文件浏览器
            dialog.show()
        }
    }

    /**
     * 从系统自带文件浏览器上选择文件的回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("hgf", "$resultCode $requestCode")
        if (resultCode == RESULT_OK && requestCode == REQUEST_CHOOSE_FILE) upload(data!!.data!!)
    }

    /**
     * 上传文件
     */
    private fun upload(uri: Uri) {
        val path = UriUtils.getFileAbsolutePath(context, uri)
        Intent(
            context,
            UploadService::class.java
        ).also { intent ->
            intent.putExtra("path", path)
            activity!!.startService(intent)
        }
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
                    startActivity(Intent(activity, SwapActivity::class.java))
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
            when (nodes[position].type) {
                FileType.DIRECTORY -> { // 是文件夹
                    refreshPath(arrayListOf(nodes[position]))
                }
                FileType.VIDEO -> {
                    val intent = Intent(context, VideoActivity::class.java)
                    intent.putExtra("node", Gson().toJson(nodes[position]))
                    startActivity(intent)
                }
                FileType.IMAGE -> {
                    DialogUtils.showImage(
                        context!!,
                        "${getString(R.string.api)}download?token=${MainApplication.getInstance().user!!.token}&nodeId=${nodes[position].nodeId}&online=1"
                    )
                }
                else -> {
                    Toast.makeText(context, "暂不支持在线查看", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 实现长按列表项的事件
     */
    inner class MyOnItemLongClickListener : RecyclerListener.OnItemLongClickListener {
        override fun onItemLongClick(view: View, position: Int) {
            fileToolbarWindow.showAsDropDown(root) // 显示底部工具栏
            fileActionbarWindow.showAtLocation(root, Gravity.START or Gravity.TOP, 0, 0) // 显示顶部操作栏
            swipeRefresh.isEnabled = false // 隐藏下拉刷新
            floatingActionButton.hide() // 隐藏右下角新建按钮
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
            move.isEnabled = (value >= 1)
            selectNumber.text = "已选中${value}个文件"
        }
    }

    /**
     * 初始化文件列表
     */
    private fun initRecycler() {
        recycler = root.findViewById(R.id.recycler)
        // 设置一个垂直方向的网格布局管理器
        recycler.layoutManager = GridLayoutManager(context, 1)
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
            adapter.changeAllItems() // 设置数据
            if (!swipeRefresh.isRefreshing) {
                loading.visibility = View.VISIBLE
            } // 如果没有下拉刷新就显示loading
            search.setText("") // 重置搜索
            val r = withContext(Dispatchers.IO) {
                HttpUtils.get("queryChildren?nodeId=${node.nodeId}")
            }
            if (r.success) {
                nodes.addAll(
                    Gson().fromJson(
                        r.msg,
                        Array<Node>::class.java
                    ) // 转为文件数组
                )
            } else {
                Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
            }
            FileUtils.formatData(nodes)
            originalNodes.clear()
            originalNodes.addAll(nodes)
            swipeRefresh.isRefreshing = false
            loading.visibility = View.GONE // 移除loading
            adapter.changeAllItems()

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
            floatingActionButton.show()
            return true
        } // 处于select模式
        if (backStack.size <= 1) return false
        backStack.removeLast()
        refreshPath()
        return true
    }

    companion object {
        private const val REQUEST_CHOOSE_FILE = 2

        @JvmStatic
        fun newInstance() =
            FileFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}