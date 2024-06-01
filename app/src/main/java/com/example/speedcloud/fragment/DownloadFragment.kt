package com.example.speedcloud.fragment

import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.MainApplication
import com.example.speedcloud.adapter.DownloadingRecyclerAdapter
import com.example.speedcloud.adapter.SwapLoadedRecyclerAdapter
import com.example.speedcloud.bean.SwapNode
import com.example.speedcloud.databinding.FragmentDownloadBinding
import com.example.speedcloud.util.DownloadManagerUtils
import java.util.*
import kotlin.collections.ArrayList


class DownloadFragment : Fragment() {

    private var downloadingNodes = MainApplication.getInstance().downloadingNodes
    private val loadedNodes: ArrayList<SwapNode> = ArrayList()
    private lateinit var loadingAdapter: DownloadingRecyclerAdapter
    private lateinit var loadedAdapter: SwapLoadedRecyclerAdapter
    private lateinit var binding: FragmentDownloadBinding
    private var swapDataBase = MainApplication.getInstance().swapDataBase
    private var isLoadingMore = false
    private var page = 0
    private lateinit var timer: Timer
    private var befSize = 0 // 之前downloadingNodes大小

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

        binding.downloadLocation.text =
            "文件下载至：${getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)?.path}"
        initRecycler()
        getUploadedNodes()
        setUploadingNodes()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                setUploadingNodes()
            }
        }, 0, 1000) // 1秒刷新一次
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
        timer.purge()
    }

    /**
     * 设置正在下载的文件信息
     */
    private fun setUploadingNodes() {
        for (node in downloadingNodes) {
            val res = DownloadManagerUtils.getDownloadProgress(node.task)
            node.speed = res.first - node.progress
            node.progress = res.first
            node.size = res.second
            node.state = res.third
        }
        activity!!.runOnUiThread {
            binding.tvLoading.text = "正在下载（${downloadingNodes.size}）"
            loadingAdapter.changeAllItems()
            if (downloadingNodes.size != befSize) {
                befSize = downloadingNodes.size
                getUploadedNodes()
            }
        }
    }

    /**
     * 初始化垂直列表
     */
    private fun initRecycler() {
        // 设置一个垂直方向的网格布局管理器
        binding.rvLoading.layoutManager = GridLayoutManager(context, 1)
        binding.rvLoaded.layoutManager = GridLayoutManager(context, 1)
        // 设置数据适配器
        loadingAdapter = DownloadingRecyclerAdapter(downloadingNodes)
        loadedAdapter = SwapLoadedRecyclerAdapter(loadedNodes)
        // 给recycler设置适配器
        binding.rvLoading.adapter = loadingAdapter
        binding.rvLoaded.adapter = loadedAdapter

        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (!isLoadingMore) {
                    isLoadingMore = true
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            activity!!.runOnUiThread {
                                ++page
                                getMoreUploadedNodes()
                                isLoadingMore = false
                            }
                        }
                    }, 250)
                }
            }
        }
    }

    /**
     * 从数据库获取下载完成的历史记录并设置
     */
    private fun getUploadedNodes() {
        loadedNodes.clear()
        loadedNodes.addAll(swapDataBase.swapNodeDao().getAllLoadedByType(false, (page + 1) * 18, 0))
        binding.tvLoaded.text = "下载完成（${loadedNodes.size}）"
        binding.loadMore.text = if (loadedNodes.size >= 18) "上拉加载更多" else "没有更多了"
        loadedAdapter.changeAllItems()
    }

    /**
     * 获取更多下载完成的历史记录
     */
    private fun getMoreUploadedNodes() {
        val res = swapDataBase.swapNodeDao().getAllLoadedByType(false, 18, page * 18)
        loadedNodes.addAll(res)
        binding.tvLoaded.text = "下载完成（${loadedNodes.size}）"
        if (res.isEmpty()) binding.loadMore.text = "没有更多了"
        loadedAdapter.notifyItemRangeChanged(loadedNodes.size - res.size, res.size)
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