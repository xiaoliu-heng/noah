package com.xiaoliublog.pic.model

import android.content.Context
import android.graphics.BitmapFactory
import com.xiaoliublog.pic.R

class NoahPro2s(var context: Context?) : Phone() {

    constructor() : this(null) {
        this.context = context
    }

    init {
        colors = mutableMapOf(
                PhoneColor.Black to BitmapFactory.decodeResource(context?.resources, R.drawable.pro2s_noah1_black),
                PhoneColor.Green to BitmapFactory.decodeResource(context?.resources, R.drawable.pro2s_noah1_green),
                PhoneColor.Pink to BitmapFactory.decodeResource(context?.resources, R.drawable.pro2s_noah1_red),
                PhoneColor.White to BitmapFactory.decodeResource(context?.resources, R.drawable.pro2s_noah1_white)
        )
        left = 115f
        top = 106f
        width = 1108f
        height = 2067f
    }

}