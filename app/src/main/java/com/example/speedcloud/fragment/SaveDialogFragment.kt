package com.example.speedcloud.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.adapter.SaveRecyclerAdapter
import com.example.speedcloud.bean.FileType
import com.example.speedcloud.bean.Node
import com.example.speedcloud.databinding.FragmentDialogSaveBinding
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.util.DialogUtil
import com.example.speedcloud.util.FileUtil
import com.example.speedcloud.util.HttpUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class SaveDialogFragment(private var clickMove: (Int) -> Unit) : DialogFragment() {
    private lateinit var adapter: SaveRecyclerAdapter
    private val nodes: ArrayList<Node> = ArrayList()
    private lateinit var binding: FragmentDialogSaveBinding
    var backStack: ArrayList<Node> = ArrayList() // 文件目录返回栈

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogSaveBinding.inflate(inflater, container, false)

        binding.btnNew.setOnClickListener {
            DialogUtil.showCreateFolderDialog(context!!) { name ->
                lifecycleScope.launch {
                    val r = withContext(Dispatchers.IO) {
                        HttpUtil.post(
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
            }
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnMove.setOnClickListener {
            clickMove(backStack.last().nodeId)
            dismiss()
        }
        binding.rvSave.layoutManager = GridLayoutManager(context, 1)
        adapter = SaveRecyclerAdapter(nodes)
        adapter.onItemClickListener = object : RecyclerListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                refreshPath(arrayListOf(nodes[position]))
            }
        }
        binding.rvSave.adapter = adapter
        backStack.add(Node("", "", 0, 0, true, 1, "全部文件", 0, FileType.DIRECTORY))
        refreshPath()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) back()
            else false
        }
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
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
            nodes.clear() // 清空之前的文件
            adapter.setItems(nodes) // 设置数据
            binding.empty.visibility = View.GONE
            binding.loading.visibility = View.VISIBLE
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
                Toast.makeText(context, r.msg, Toast.LENGTH_SHORT).show()
            }
            nodes.sortByDescending { it.isDirectory }
            while (nodes.isNotEmpty()) {
                if (!nodes.last().isDirectory) nodes.removeLast()
                else break
            }
            FileUtil.formatData(nodes)
            if (nodes.isEmpty()) binding.empty.visibility = View.VISIBLE
            binding.loading.visibility = View.GONE // 移除loading
            adapter.setItems(nodes)
        }
    }

    /**
     * 返回能否回退上一层，能就直接回退上一层。
     * @return true为可以回退，false为已经到达根目录，无法回退，即将退出应用
     */
    private fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeLast()
        refreshPath()
        return true
    }
}