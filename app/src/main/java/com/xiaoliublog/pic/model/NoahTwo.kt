package com.xiaoliublog.pic.model

import android.content.Context
import com.xiaoliublog.pic.R
import com.xiaoliublog.pic.utils.getDeviceName

class NoahTwo(override var context: Context) : Phone(context) {
    override val colors: MutableMap<PhoneColor, Int?>
        get() {
            return when (getDeviceName()) {
                "OS105",
                "OE106" -> mutableMapOf(
                        PhoneColor.Black to R.drawable.noah2_pro2_black_black,
                        PhoneColor.White to R.drawable.noah2_pro2_white_white,
                        PhoneColor.BlackOnWhite to R.drawable.noah2_pro2_black_white,
                        PhoneColor.WhiteOnBlack to R.drawable.noah2_pro2_white_black
                )
                "DE106" -> mutableMapOf(
                        PhoneColor.Black to R.drawable.noah2_r1_black_black,
                        PhoneColor.White to R.drawable.noah2_r1_white_white,
                        PhoneColor.BlackOnWhite to R.drawable.noah2_r1_black_white,
                        PhoneColor.WhiteOnBlack to R.drawable.noah2_r1_white_black
                )
                else -> mutableMapOf(
                        PhoneColor.Black to R.drawable.noah2_pro2_black_black,
                        PhoneColor.White to R.drawable.noah2_pro2_white_white,
                        PhoneColor.BlackOnWhite to R.drawable.noah2_pro2_black_white,
                        PhoneColor.WhiteOnBlack to R.drawable.noah2_pro2_white_black
                )
            }
        }

    init {
        left = 540f
        top = 700f
        width = 2157f
        height = 3890f
    }
}