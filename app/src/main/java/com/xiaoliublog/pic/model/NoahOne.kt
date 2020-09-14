package com.xiaoliublog.pic.model

import android.content.Context
import com.xiaoliublog.pic.R


class NoahOne(override var context: Context) : Phone(context) {
    init {
        colors = mutableMapOf(
                PhoneColor.Black to R.drawable.noah1_black,
                PhoneColor.Green to R.drawable.noah1_green,
                PhoneColor.Pink to R.drawable.noah1_red,
                PhoneColor.White to R.drawable.noah1_white
        )
        left = 72f
        top = 70f
        width = 2157f
        height = 3890f

        topOfHeight = 0.0332541567695962F
    }

}