package com.xiaoliublog.pic.model

import android.content.Context
import com.xiaoliublog.pic.R


class NoahOne(override var context: Context) : Phone(context) {
    init {
        colors = mutableMapOf(
                PhoneColor.Black to R.drawable.pro2s_noah1_black,
                PhoneColor.Green to R.drawable.pro2s_noah1_green,
                PhoneColor.Pink to R.drawable.pro2s_noah1_red,
                PhoneColor.White to R.drawable.pro2s_noah1_white
        )
        left = 70f
        top = 65f
        width = 1100f
        height = 2051f

        topOfHeight = 0.0332541567695962F
    }

}