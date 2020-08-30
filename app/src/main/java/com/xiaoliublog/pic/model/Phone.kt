package com.xiaoliublog.pic.model

import android.graphics.Bitmap

enum class PhoneColor {
    Green,
    White,
    Black,
    Pink,
    BlackOnWhite,
    WhiteOnBlack
}

open class Phone {
    lateinit var colors: MutableMap<PhoneColor, Bitmap>
    var top: Float = 0f
    var left: Float = 0f
    var height: Float = 0f
    var width: Float = 0f
    var right: Float = 0f
    var bottom: Float = 0f
}