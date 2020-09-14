package com.xiaoliublog.pic.utils

import android.view.View

fun animation(view: View) {
    view.animate().apply {
        duration = 100
        scaleX(0.8f)
        scaleY(0.8f)
        withEndAction {
            scaleX(1f)
            scaleY(1f)
            start()
        }
        start()
    }
}