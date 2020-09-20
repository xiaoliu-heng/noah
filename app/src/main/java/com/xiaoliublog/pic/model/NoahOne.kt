package com.xiaoliublog.pic.model

import android.content.Context
import com.xiaoliublog.pic.R
import com.xiaoliublog.pic.utils.getDeviceName


class NoahOne(override var context: Context) : Phone(context) {
    override val colors: MutableMap<PhoneColor, Int?>
        get() {
            return when (getDeviceName()) {
                "OS105",
                "OC105",
                "OE106" -> mutableMapOf(
                        PhoneColor.Black to R.drawable.noah1_pro2_black,
                        PhoneColor.Green to R.drawable.noah1_pro2_green,
                        PhoneColor.Pink to R.drawable.noah1_pro2_red,
                        PhoneColor.White to R.drawable.noah1_pro2_white
                )
                "DE106" -> mutableMapOf(
                        PhoneColor.Black to R.drawable.noah1_r1_black,
                        PhoneColor.Green to R.drawable.noah1_r1_green,
                        PhoneColor.Pink to R.drawable.noah1_r1_red,
                        PhoneColor.White to R.drawable.noah1_r1_white
                )
                else -> mutableMapOf(
                        PhoneColor.Black to R.drawable.noah1_pro2_black,
                        PhoneColor.Green to R.drawable.noah1_pro2_green,
                        PhoneColor.Pink to R.drawable.noah1_pro2_red,
                        PhoneColor.White to R.drawable.noah1_pro2_white
                )
            }
        }

    override val top: Float
        get() = 65f

    override val left: Float
        get() = 78f

    override val paddingTop: Float
        get() {
            return when (getDeviceName()) {
                "OS105",
                "OE106" -> 706f
                "DE106" -> 676f
                else -> 700f
            }
        }

    override val paddingLeft: Float
        get() {
            return when (getDeviceName()) {
                "OS105",
                "OE106" -> 463f
                "DE106" -> 463f
                else -> 463f
            }
        }

    init {
        width = 2157f
        height = 3890f
    }

}