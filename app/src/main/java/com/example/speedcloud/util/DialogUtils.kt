package com.example.speedcloud.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.speedcloud.R

object DialogUtils {
    /**
     * 生成提醒对话框
     */
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        onClickListener: View.OnClickListener
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null)
        val dialog = AlertDialog.Builder(context).setCancelable(false).setView(view).create()
        view.findViewById<TextView>(R.id.title).text = title
        view.findViewById<TextView>(R.id.message).text = message
        view.findViewById<TextView>(R.id.cancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<TextView>(R.id.confirm).setOnClickListener {
            dialog.dismiss()
            onClickListener.onClick(it)
        }
        val displayRectangle = Rect()
        dialog.window!!.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        dialog.window!!.setLayout(
            (displayRectangle.width() * 0.75).toInt(),
            dialog.window!!.attributes.height
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * 创建新建文件夹对话框
     */
    fun showCreateFolderDialog(context: Context, clickPositive: (String) -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null)
        val editText = view.findViewById<EditText>(R.id.editText)
        editText.hint = "输入文件夹名字"
        val dialog = AlertDialog.Builder(context).setTitle("新建文件夹").setView(view)
            .setPositiveButton("创建") { dialog, _ ->
                dialog.dismiss()
                clickPositive(editText.text.toString())
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.create()
        dialog.show()
        dialog.getButton(Dialog.BUTTON_NEGATIVE)
            .setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
    }

    /**
     * 展示图片
     */
    fun showImage(context: Context, url: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_image, null)
        val image = view.findViewById<ImageView>(R.id.imageView)
        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val dialog = Dialog(context)
        loading.visibility = View.VISIBLE
        view.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.show()
        Glide.with(context).load(url).listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    loading.visibility = View.GONE
                    return false
                }
            }
        ).into(image)
    }

    /**
     * 查看分享链接对话框
     */
    fun showShareDialog(
        context: Context,
        code: String?,
        onClickListener: View.OnClickListener
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_share_link_look, null)
        val dialog = AlertDialog.Builder(context).setCancelable(false).setView(view).create()
        if (code != null) {
            view.findViewById<TextView>(R.id.code).also {
                it.visibility = View.VISIBLE
                it.text = "提取码：${code}（来自剪贴板）"
            }
        } else view.findViewById<TextView>(R.id.code).visibility = View.GONE
        view.findViewById<ImageButton>(R.id.close).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.look).setOnClickListener {
            dialog.dismiss()
            onClickListener.onClick(it)
        }
        val displayRectangle = Rect()
        dialog.window!!.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        dialog.window!!.setLayout(
            (displayRectangle.width() * 0.80).toInt(),
            dialog.window!!.attributes.height
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}