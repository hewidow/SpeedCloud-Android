package com.example.speedcloud

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.adapter.RecycleBinRecyclerAdapter
import com.example.speedcloud.bean.Node
import com.example.speedcloud.databinding.ActivityRecycleBinBinding
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.util.DialogUtils
import com.example.speedcloud.util.FileUtils
import com.example.speedcloud.util.HttpUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class RecycleBinActivity : AppCompatActivity() {
    private lateinit var adapter: RecycleBinRecyclerAdapter
    private val nodes: ArrayList<Node> = ArrayList()
    private lateinit var binding: ActivityRecycleBinBinding
    private lateinit var bottomToolbar: PopupWindow
    private var selectedItem: ArrayList<Node> = ArrayList() // 选择的文件项
    private lateinit var selectedNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycleBinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 底部工具栏
        val view = layoutInflater.inflate(R.layout.dialog_recycle_bin, null)
        selectedNumber = view.findViewById(R.id.selectedNumber)
        view.findViewById<Button>(R.id.reduce).setOnClickListener {
            getSelectedItem()
            lifecycleScope.launch {
                val r = withContext(Dispatchers.IO) {
                    HttpUtils.post("recovery", Gson().toJson(selectedItem.map { it.nodeId }))
                }
                if (r.success) {
                    refresh()
                } else {
                    Toast.makeText(this@RecycleBinActivity, r.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        view.findViewById<Button>(R.id.delete).setOnClickListener {
            getSelectedItem()
            DialogUtils.showAlertDialog(this@RecycleBinActivity, "提示", "确认彻底删除选中的文件") {
                lifecycleScope.launch {
                    val r = withContext(Dispatchers.IO) {
                        HttpUtils.post("deleteFinal", Gson().toJson(selectedItem.map { it.nodeId }))
                    }
                    if (r.success) {
                        refresh()
                    } else {
                        Toast.makeText(this@RecycleBinActivity, r.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        bottomToolbar = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        bottomToolbar.animationStyle = R.style.popup_window_bottom_top_anim

        // 列表项
        adapter = RecycleBinRecyclerAdapter(nodes)
        adapter.onSelectedItemNumberChangeListener =
            object : RecyclerListener.OnSelectedItemNumberChangeListener {
                override fun onSelectedItemNumberChange(value: Int) {
                    if (value > 0) bottomToolbar.showAtLocation(
                        binding.root,
                        Gravity.START or Gravity.BOTTOM,
                        0,
                        0
                    )
                    else bottomToolbar.dismiss()
                    if (value == nodes.size) {
                        binding.selectAll.visibility = View.GONE
                        binding.unselectAll.visibility = View.VISIBLE
                    }
                    if (value == 0) {
                        binding.unselectAll.visibility = View.GONE
                        binding.selectAll.visibility = View.VISIBLE
                    }
                    selectedNumber.text = "已选中${value}个文件"
                }
            }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
        binding.recyclerView.adapter = adapter

        // 顶部工具栏返回按钮
        binding.toolbar.setNavigationOnClickListener { finish() }
        // 顶部工具栏全选和全不选
        binding.selectAll.setOnClickListener {
            it.visibility = View.GONE
            binding.unselectAll.visibility = View.VISIBLE
            adapter.selectAllOrNot(true)
        }
        binding.unselectAll.setOnClickListener {
            it.visibility = View.GONE
            binding.selectAll.visibility = View.VISIBLE
            adapter.selectAllOrNot(false)
        }

        refresh()
    }

    /**
     * 刷新回收站界面
     */
    private fun refresh() {
        lifecycleScope.launch {
            binding.loading.visibility = View.VISIBLE
            nodes.clear()
            val r = withContext(Dispatchers.IO) {
                HttpUtils.get("recycle")
            }
            if (r.success) {
                nodes.addAll(
                    Gson().fromJson(
                        r.msg,
                        Array<Node>::class.java
                    ) // 转为文件数组
                )
            } else {
                Toast.makeText(this@RecycleBinActivity, r.msg, Toast.LENGTH_SHORT).show()
            }
            FileUtils.formatData(nodes)
            FileUtils.sortDataByDeleteDate(nodes)
            binding.loading.visibility = View.GONE
            adapter.changeAllItems()
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
}