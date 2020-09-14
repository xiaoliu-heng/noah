package com.xiaoliublog.pic

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.lifecycle.LiveData
import com.xiaoliublog.pic.ui.MyImageView

@BindingConversion
fun convertColorToDrawable(color: Int) = ColorDrawable(color)

@BindingAdapter("background_color")
fun setBgColor(view: View, color: LiveData<Int?>) {
    view.setBackgroundColor(color.value!!)
}

@BindingAdapter("bitmap")
fun setBitmap(imageView: MyImageView, src: Bitmap?) {
    imageView.setImageBitmap(src)
}

@BindingAdapter("bg_img")
fun setBgImg(view: View, img: Drawable) {
    Log.d("hyl", "setBgImg: $img")
    view.background = img
}