package com.example.speedcloud

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.speedcloud.bean.Node
import com.example.speedcloud.bean.PlayUrl
import com.example.speedcloud.databinding.ActivityVideoBinding
import com.example.speedcloud.util.HttpUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoBinding
    private lateinit var node: Node
    private lateinit var mediaController: MediaController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        node = Gson().fromJson(intent.getStringExtra("node"), Node::class.java)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        lifecycleScope.launch {
            binding.loading.visibility = View.VISIBLE
            val r = withContext(Dispatchers.IO) {
                HttpUtil.get("playVideo?nodeId=${node.nodeId}")
            }
            if (r.success) {
                val url = Gson().fromJson(r.msg, PlayUrl::class.java).url
                binding.videoView.setVideoPath(url)
                binding.videoView.setOnPreparedListener {
                    binding.loading.visibility = View.GONE
                }
                mediaController = MediaController(this@VideoActivity)
                binding.videoView.setMediaController(mediaController)
                mediaController.setMediaPlayer(binding.videoView)
                binding.videoView.start()
            } else {
                Toast.makeText(this@VideoActivity, "无法播放", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}