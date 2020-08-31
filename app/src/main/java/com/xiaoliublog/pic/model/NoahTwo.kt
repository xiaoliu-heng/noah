package com.xiaoliublog.pic.model

import android.content.Context
import com.xiaoliublog.pic.R

class NoahTwo(override var context: Context) : Phone(context) {

    init {
        colors = mutableMapOf(
                PhoneColor.Black to R.drawable.noah2_black_black,
                PhoneColor.White to R.drawable.noah2_white_white,
                PhoneColor.BlackOnWhite to R.drawable.noah2_black_white,
                PhoneColor.WhiteOnBlack to R.drawable.noah2_white_black
        )
        left = 790f
        top = 430f
        width = 2200f
        height = 2200f
    }
}