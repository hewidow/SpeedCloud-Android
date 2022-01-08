package com.example.speedcloud.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.speedcloud.R

object DialogUtil {
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
}