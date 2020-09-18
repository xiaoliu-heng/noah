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
    open val colors: MutableMap<PhoneColor, Int?> = mutableMapOf()
    private var cache: MutableMap<PhoneColor, Bitmap?> = mutableMapOf()
    open val top: Float = 0f
    open val left: Float = 0f
    var height: Float = 0f
    var width: Float = 0f
    open val paddingTop: Float = 0f
    open val paddingLeft: Float = 0f
    fun load(color: PhoneColor): Bitmap? {
        val phone = colors.get(color) ?: return null
        val bitmap: Bitmap?
        if (cache[color] == null) {
            bitmap = BitmapFactory.decodeResource(context.resources, phone)
            cache[color] = bitmap
        } else {
            bitmap = cache[color]
        }
        return bitmap
    }
}