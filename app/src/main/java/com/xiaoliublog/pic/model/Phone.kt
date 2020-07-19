package com.xiaoliublog.pic.model

import android.graphics.Bitmap

enum class PhoneColor {
    Green,
    White,
    Black,
    Pink
}

open class Phone {
    lateinit var colors: MutableMap<PhoneColor, Bitmap>
    val top: Int = 0
    val right: Int = 0
    val bottom: Int = 0
    val left: Int = 0
}