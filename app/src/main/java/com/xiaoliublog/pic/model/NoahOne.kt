package com.xiaoliublog.pic.model

import android.content.Context
import android.graphics.BitmapFactory
import com.xiaoliublog.pic.R

class NoahOne(var context: Context) : Phone() {

    init {
        colors = mutableMapOf(
                PhoneColor.Black to BitmapFactory.decodeResource(context.resources, R.drawable.pro2s_noah1_black),
                PhoneColor.Green to BitmapFactory.decodeResource(context.resources, R.drawable.pro2s_noah1_green),
                PhoneColor.Pink to BitmapFactory.decodeResource(context.resources, R.drawable.pro2s_noah1_red),
                PhoneColor.White to BitmapFactory.decodeResource(context.resources, R.drawable.pro2s_noah1_white)
        )
        left = 70f
        top = 65f
        width = 1100f
        height = 2067f
    }

}