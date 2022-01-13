package com.example.speedcloud.fragment

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.MainApplication
import com.example.speedcloud.adapter.SwapLoadedRecyclerAdapter
import com.example.speedcloud.adapter.UploadingRecyclerAdapter
import com.example.speedcloud.bean.SwapNode
import com.example.speedcloud.databinding.FragmentUploadBinding
import com.example.speedcloud.listener.RecyclerListener
import com.example.speedcloud.service.UploadService
import java.util.*
import kotlin.collections.ArrayList


// https://developer.android.google.cn/guide/components/bound-services?hl=zh-cn#Binding
class UploadFragment : Fragment() {

    private var page: Int = 0
    private var isLoadingMore: Boolean = false
    private var uploadingNodes = MainApplication.getInstance().uploadingNodes
    private val loadedNodes: ArrayList<SwapNode> = ArrayList()
    private lateinit var loadingAdapter: UploadingRecyclerAdapter
    private lateinit var loadedAdapter: SwapLoadedRecyclerAdapter
    private lateinit var binding: FragmentUploadBinding
    private var swapDataBase = MainApplication.getInstance().swapDataBase
    private var mService: UploadService? = null
    private var bound: Boolean = false
    private var befTimeStamp: Long = 0
    private var speed: Long = 0
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as UploadService.UploadBinder
            mService = binder.getService()
            mService!!.onProgressChangeListener =
                object : RecyclerListener.OnProgressChangeListener {
                    override fun onProgressChange() {
                        activity?.runOnUiThread { // 在ui线程改变界面
                            setUploadingNodes()
                        }
                    }
                }
            mService!!.onCompleteTransferListener =
                object : RecyclerListener.OnCompleteTransferListener {
                    override fun onCompleteTransfer() {
                        activity?.runOnUiThread {
                            setUploadingNodes()
                            getUploadedNodes()
                        }
                    }
                }
            bound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService!!.onProgressChangeListener = null // 解除监听器
            mService!!.onCompleteTransferListener = null
            mService = null
            bound = false
        }
    }

    /**
     * 设置正在上传节点
     */
    fun setUploadingNodes() {
        binding.tvLoading.text = "正在上传（${uploadingNodes.size}）"
        loadingAdapter.changeAllItems()
    }

    override fun onStart() {
        super.onStart()
        // 绑定服务
        Intent(context, UploadService::class.java).also { intent ->
            activity!!.bindService(intent, mConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // 解绑服务
        if (bound) {
            activity!!.unbindService(mConnection)
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadBinding.inflate(inflater, container, false)

        initRecycler()
        setUploadingNodes()
        getUploadedNodes()
        return binding.root
    }


    /**
     * 初始化垂直列表
     */
    private fun initRecycler() {
        // 设置一个垂直方向的网格布局管理器
        binding.rvLoading.layoutManager = GridLayoutManager(context, 1)
        binding.rvLoaded.layoutManager = GridLayoutManager(context, 1)
        // 设置数据适配器
        loadingAdapter = UploadingRecyclerAdapter(uploadingNodes)
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
     * 从数据库获取上传完成的历史记录并设置
     */
    private fun getUploadedNodes() {
        loadedNodes.clear()
        loadedNodes.addAll(swapDataBase.swapNodeDao().getAllLoadedByType(true, (page + 1) * 18, 0))
        binding.tvLoaded.text = "上传完成（${loadedNodes.size}）"
        binding.loadMore.text = if (loadedNodes.size >= 18) "上拉加载更多" else "没有更多了"
        loadedAdapter.changeAllItems()
    }

    /**
     * 获取更多节点
     */
    private fun getMoreUploadedNodes() {
        val res = swapDataBase.swapNodeDao().getAllLoadedByType(true, 18, page * 18)
        loadedNodes.addAll(res)
        binding.tvLoaded.text = "上传完成（${loadedNodes.size}）"
        if (res.isEmpty()) binding.loadMore.text = "没有更多了"
        loadedAdapter.notifyItemRangeChanged(loadedNodes.size - res.size, res.size)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            UploadFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}