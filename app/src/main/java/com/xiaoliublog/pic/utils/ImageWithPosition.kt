package com.xiaoliublog.pic.utils

import android.graphics.Bitmap

data class ImageWithPosition (
        var left: Float = 0f,
        var top: Float = 0f,
        var image: Bitmap? = null
)