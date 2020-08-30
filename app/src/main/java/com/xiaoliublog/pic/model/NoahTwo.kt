package com.xiaoliublog.pic.model

import android.content.Context
import android.graphics.BitmapFactory
import com.xiaoliublog.pic.R

class NoahTwo(var context: Context) : Phone() {

    init {
        colors = mutableMapOf(
                PhoneColor.Black to BitmapFactory.decodeResource(context.resources, R.drawable.noah2_black_black),
                PhoneColor.White to BitmapFactory.decodeResource(context.resources, R.drawable.noah2_white_white),
                PhoneColor.BlackOnWhite to BitmapFactory.decodeResource(context.resources, R.drawable.noah2_black_white),
                PhoneColor.WhiteOnBlack to BitmapFactory.decodeResource(context.resources, R.drawable.noah2_white_black)
        )
        left = 790f
        top = 430f
        width = 2200f
        height = 2200f
    }

}