package com.example.speedcloud.fragment

import android.app.DownloadManager
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.MainApplication
import com.example.speedcloud.adapter.SwapLoadedRecyclerAdapter
import com.example.speedcloud.adapter.SwapLoadingRecyclerAdapter
import com.example.speedcloud.bean.SwapNode
import com.example.speedcloud.databinding.FragmentDownloadBinding
import kotlinx.coroutines.*


class DownloadFragment : Fragment() {

    private lateinit var job: Job
    private val loadingNodes: ArrayList<SwapNode> = ArrayList()
    private val loadedNodes: ArrayList<SwapNode> = ArrayList()
    private lateinit var loadingAdapter: SwapLoadingRecyclerAdapter
    private lateinit var loadedAdapter: SwapLoadedRecyclerAdapter
    private lateinit var binding: FragmentDownloadBinding
    private var swapDataBase = MainApplication.getInstance().swapDataBase
    private var downloadManager = MainApplication.getInstance().downloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 使用viewBinding绑定
        binding = FragmentDownloadBinding.inflate(inflater, container, false)

        loadingNodes.addAll(swapDataBase.swapNodeDao().getAllLoadingByType(false))
        loadedNodes.addAll(swapDataBase.swapNodeDao().getAllLoadedByType(false))
        initRecycler()
        binding.downloadLocation.text =
            "文件下载至：${getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)?.path}"
        initTimedTask()
        return binding.root
    }

    /**
     * 简单起见，初始化一个协程每秒刷新，全部一起查询和更新
     */
    private fun initTimedTask() {
        lifecycleScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    loadingNodes.clear()
                    loadedNodes.clear()
                    loadingNodes.addAll(swapDataBase.swapNodeDao().getAllLoadingByType(false))
                    loadedNodes.addAll(swapDataBase.swapNodeDao().getAllLoadedByType(false))
                    for (i in loadingNodes.indices) {
                        val res = getDownloadProgress(loadingNodes[i].task)
                        loadingNodes[i].speed = res.first - loadingNodes[i].progress
                        loadingNodes[i].progress = res.first
                        loadingNodes[i].size = res.second
                        loadingNodes[i].state = res.third
                    }
                    swapDataBase.swapNodeDao().updateAll(*loadingNodes.toTypedArray())
                }
                binding.tvLoading.text = "正在下载（${loadingNodes.size}）"
                binding.tvLoaded.text = "下载完成（${loadedNodes.size}）"
                loadingAdapter.setItems(loadingNodes)
                loadedAdapter.setItems(loadedNodes)
                delay(1000) // 1秒更新一次
            }
        }
    }

    /**
     * 根据id从DownloadManager中查询下载进度
     */
    private fun getDownloadProgress(id: Long): Triple<Long, Long, Int> {
        val query = DownloadManager.Query().setFilterById(id)
        downloadManager.query(query)?.use { c ->
            if (c.moveToFirst()) {
                return Triple(
                    c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
                    c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                )
            }
        }
        return Triple(0, 0, 0)
    }

    /**
     * 初始化垂直列表
     */
    private fun initRecycler() {
        // 设置一个垂直方向的网格布局管理器
        binding.rvLoading.layoutManager = GridLayoutManager(this.activity, 1)
        binding.rvLoaded.layoutManager = GridLayoutManager(this.activity, 1)
        // 设置数据适配器
        loadingAdapter = SwapLoadingRecyclerAdapter(loadingNodes)
        loadedAdapter = SwapLoadedRecyclerAdapter(loadedNodes)
        // 给recycler设置适配器
        binding.rvLoading.adapter = loadingAdapter
        binding.rvLoaded.adapter = loadedAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DownloadFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}