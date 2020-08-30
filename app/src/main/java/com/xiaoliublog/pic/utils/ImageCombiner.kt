package com.xiaoliublog.pic.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import java.util.function.Consumer

class ImageCombiner(
        var images: List<ImageWithPosition> = ArrayList()
) {

    private val canvas: Canvas = Canvas()
    private val paint: Paint = Paint().apply {
        strokeWidth = 4f
        color = Color.RED
    }
    private val paintShadow: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        setShadowLayer(12f, 0f, 0f, Color.GRAY)
    }
    var width = 0
    var height = 0
    var bgColor = 0

    fun combine(): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true)
        canvas.setBitmap(result)
        canvas.drawColor(bgColor)
        images.forEach(Consumer { i: ImageWithPosition -> canvas.drawBitmap(i.image, i.left, i.top, paint) })
//        for (i in 0..width step 10) {
//            if (i%100f==0f){
//                paint.color = Color.GREEN
//                canvas.drawPoint(i.toFloat(), height / 2f, paint)
//            }else{
//                paint.color = Color.RED
//                canvas.drawPoint(i.toFloat(), height / 2f, paint)
//            }
//        }
//        for (i in 0..height step 10) {
//            if (i%100f==0f){
//                paint.color = Color.GREEN
//                canvas.drawPoint(width/2f, i.toFloat(), paint)
//            }else{
//                paint.color = Color.RED
//                canvas.drawPoint(width/2f, i.toFloat(), paint)
//            }
//        }
        canvas.save()
        canvas.restore()
        return result
    }
}