package com.example.speedcloud.util

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.speedcloud.MainApplication
import com.example.speedcloud.R
import com.example.speedcloud.bean.FileType

object FileTypeUtils {
    private var icons: Array<Drawable> = arrayOf(
        ContextCompat.getDrawable(
            MainApplication.getInstance().applicationContext,
            R.drawable.ic_baseline_folder_24
        )!!,
        ContextCompat.getDrawable(
            MainApplication.getInstance().applicationContext,
            R.drawable.ic_baseline_videocam_24
        )!!,
        ContextCompat.getDrawable(
            MainApplication.getInstance().applicationContext,
            R.drawable.ic_baseline_image_24
        )!!,
        ContextCompat.getDrawable(
            MainApplication.getInstance().applicationContext,
            R.drawable.ic_baseline_insert_drive_file_24
        )!!
    )
    private var iconsColor: Array<Int> = arrayOf(
        ContextCompat.getColor(
            MainApplication.getInstance().applicationContext,
            R.color.icon_folder
        ),
        ContextCompat.getColor(
            MainApplication.getInstance().applicationContext,
            R.color.icon_video
        ),
        ContextCompat.getColor(
            MainApplication.getInstance().applicationContext,
            R.color.icon_image
        ),
        ContextCompat.getColor(
            MainApplication.getInstance().applicationContext,
            R.color.icon_file
        )
    )

    fun getIconDrawableAndColor(type: FileType): Pair<Drawable, Int> {
        return Pair(icons[type.ordinal], iconsColor[type.ordinal])
    }
}