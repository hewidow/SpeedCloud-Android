package com.example.speedcloud.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R

class MeFragment : Fragment() {

    private lateinit var root: View
    private var user = MainApplication.getInstance().user!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_me, container, false)
        root.findViewById<TextView>(R.id.tv_name).text = user.userDetail.username
        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}