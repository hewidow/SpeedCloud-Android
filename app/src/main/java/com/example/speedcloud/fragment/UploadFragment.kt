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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.MainApplication
import com.example.speedcloud.adapter.SwapLoadedRecyclerAdapter
import com.example.speedcloud.adapter.UploadingRecyclerAdapter
import com.example.speedcloud.bean.SwapNode
import com.example.speedcloud.bean.UploadingNode
import com.example.speedcloud.databinding.FragmentUploadBinding
import com.example.speedcloud.listener.OnProgressListener
import com.example.speedcloud.service.UploadService


// https://developer.android.google.cn/guide/components/bound-services?hl=zh-cn#Binding
class UploadFragment : Fragment() {

    private var loadingNodes: ArrayList<UploadingNode> = ArrayList()
    private val loadedNodes: ArrayList<SwapNode> = ArrayList()
    private lateinit var loadingAdapter: UploadingRecyclerAdapter
    private lateinit var loadedAdapter: SwapLoadedRecyclerAdapter
    private lateinit var binding: FragmentUploadBinding
    private var swapDataBase = MainApplication.getInstance().swapDataBase
    private var mService: UploadService? = null
    private var bound: Boolean = false
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as UploadService.UploadBinder
            mService = binder.getService()
            mService!!.onProgressListener = object : OnProgressListener {
                override fun onProgressChange() {
                    activity!!.runOnUiThread { // 在ui线程改变界面
                        setUploadingNodes()
                        getUploadedNodes()
                    }
                }
            }
            bound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
            bound = false
        }
    }

    /**
     * 设置正在上传节点
     */
    fun setUploadingNodes() {
        loadingNodes = MainApplication.getInstance().uploadingNodes
        binding.tvLoaded.text = "正在上传（${loadingNodes.size}）"
        loadingAdapter.setItems(loadingNodes)
    }

    override fun onStart() {
        super.onStart()
        Intent(context, UploadService::class.java).also { intent ->
            activity!!.bindService(intent, mConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
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
        getUploadedNodes()
        setUploadingNodes()
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
        loadingAdapter = UploadingRecyclerAdapter(loadingNodes)
        loadedAdapter = SwapLoadedRecyclerAdapter(loadedNodes)
        // 给recycler设置适配器
        binding.rvLoading.adapter = loadingAdapter
        binding.rvLoaded.adapter = loadedAdapter
    }

    /**
     * 从数据库获取上传完成的记录并设置
     */
    private fun getUploadedNodes() {
        loadedNodes.clear()
        loadedNodes.addAll(swapDataBase.swapNodeDao().getAllLoadedByType(true))
        binding.tvLoaded.text = "上传完成（${loadedNodes.size}）"
        loadedAdapter.setItems(loadedNodes)
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