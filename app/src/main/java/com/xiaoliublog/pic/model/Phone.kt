package com.xiaoliublog.pic.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

enum class PhoneColor {
    Green,
    White,
    Black,
    Pink,
    BlackOnWhite,
    WhiteOnBlack
}

open class Phone(open val context: Context) {
    lateinit var colors: MutableMap<PhoneColor, Int?>
    var phones: MutableMap<PhoneColor, Bitmap?> = mutableMapOf()
    var top: Float = 0f
    var left: Float = 0f
    var height: Float = 0f
    var width: Float = 0f
    var topOfHeight: Float = 0f
    var leftOfWidth: Float = 0f
    fun load(color: PhoneColor): Bitmap? {
        val phone = colors.get(color) ?: return null
        val bitmap: Bitmap?
        if (phones.get(color) == null) {
            bitmap = BitmapFactory.decodeResource(context.resources, phone)
            phones[color] = bitmap
        } else {
            bitmap = phones.get(color)
        }
        return bitmap
    }
}