package com.example.speedcloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.speedcloud.adapter.SwapRecyclerAdapter
import com.example.speedcloud.bean.DownloadNode
import com.example.speedcloud.databinding.FragmentDownloadBinding


class DownloadFragment : Fragment() {

    private val nodes: ArrayList<DownloadNode> = ArrayList()
    private lateinit var adapter: SwapRecyclerAdapter
    private lateinit var binding: FragmentDownloadBinding

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

        nodes.add(DownloadNode("2021-1-1 17:16", 1, 12314124, "filename", 12414))
        nodes.add(DownloadNode("2021-1-1 17:16", 2, 12414, "filename", 12414))
        initRecycler()

        return binding.root
    }

    private fun initRecycler() {
        // 设置一个垂直方向的网格布局管理器
        binding.recyclerView.layoutManager = GridLayoutManager(this.activity, 1)
        // 设置数据适配器
        adapter = SwapRecyclerAdapter(nodes)
        // 给recycler设置适配器
        binding.recyclerView.adapter = adapter
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