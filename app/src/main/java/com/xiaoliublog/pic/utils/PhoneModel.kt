package com.xiaoliublog.pic.utils

import android.os.Build

fun getDeviceName(): String? {
    val model = Build.MODEL
    return model.capitalize()
}