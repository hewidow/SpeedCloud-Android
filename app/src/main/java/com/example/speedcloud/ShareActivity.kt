package com.example.speedcloud

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.adapter.ShareRecyclerAdapter
import com.example.speedcloud.bean.Node
import com.example.speedcloud.bean.Sharer
import com.example.speedcloud.databinding.ActivityShareBinding
import com.example.speedcloud.fragment.SaveDialogFragment
import com.example.speedcloud.util.FileUtils
import com.example.speedcloud.util.HttpUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareActivity : AppCompatActivity() {
    private lateinit var adapter: ShareRecyclerAdapter
    private var code: String? = null
    private var uniqueId: String? = null
    private lateinit var binding: ActivityShareBinding
    private val nodes: ArrayList<Node> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uniqueId = intent.getStringExtra("uniqueId")
        code = intent.getStringExtra("code")

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ShareRecyclerAdapter(nodes)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
        binding.recyclerView.adapter = adapter


        getSharerName()
        code?.also {
            binding.code.setText(code)
            extract()
        }

        binding.extract.setOnClickListener {
            code = binding.code.text.toString()
            extract()
        }

        binding.save.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val newFragment = SaveDialogFragment("选择保存位置", "保存至此") { id ->
                lifecycleScope.launch {
                    val r = withContext(Dispatchers.IO) {
                        HttpUtils.post(
                            "save", Gson().toJson(
                                mapOf(
                                    "dstNodeId" to id,
                                    "srcNodeId" to nodes.map { it.nodeId },
                                    "uniqueId" to uniqueId
                                )
                            )
                        )
                    }
                    if (r.success) Toast.makeText(this@ShareActivity, "保存成功", Toast.LENGTH_SHORT)
                        .show()
                    else Toast.makeText(this@ShareActivity, r.msg, Toast.LENGTH_SHORT).show()
                }
            }
            newFragment.show(fragmentManager, "saveDialog")
        }
    }

    /**
     * 提取文件
     */
    private fun extract() {
        lifecycleScope.launch {
            binding.extract.isEnabled = false
            val r = withContext(Dispatchers.IO) {
                HttpUtils.post(
                    "check", Gson().toJson(
                        mapOf(
                            "checkCode" to code,
                            "uniqueId" to uniqueId
                        )
                    )
                )
            }
            if (r.success) {
                nodes.addAll(
                    Gson().fromJson(
                        r.msg,
                        Array<Node>::class.java
                    )
                )
                FileUtils.formatData(nodes)
                binding.codePage.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.bottomToolbar.visibility = View.VISIBLE
                adapter.changeAllItems()
            } else {
                Toast.makeText(this@ShareActivity, r.msg, Toast.LENGTH_SHORT).show()
            }
            binding.extract.isEnabled = true
        }
    }

    /**
     * 获取分享者名字
     */
    private fun getSharerName() {
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) {
                HttpUtils.post(
                    "travel", Gson().toJson(
                        mapOf(
                            "uniqueId" to uniqueId
                        )
                    )
                )
            }
            if (r.success) {
                val sharer = Gson().fromJson(r.msg, Sharer::class.java)
                binding.username.text = sharer.username
            } else {
                Toast.makeText(this@ShareActivity, r.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}