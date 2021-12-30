package com.example.speedcloud.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.speedcloud.R
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

class FileFragment : Fragment(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var root: View
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_file, container, false)
        toolbar = root.findViewById(R.id.toolbar)
        root.findViewById<AppBarLayout>(R.id.appbar)
            .addOnOffsetChangedListener(this) // 监听appbar处于折叠还是展开
        return root
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
        }
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