package com.xiaoliublog.pic.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.util.function.Consumer

class ImageCombiner(
        var images: List<ImageWithPosition> = ArrayList()
) {

    private val canvas: Canvas = Canvas()
    private val paint: Paint = Paint()
    var width = 0
    var height = 0
    var bgColor = 0

    fun combine(): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true)
        canvas.setBitmap(result)
        canvas.drawColor(bgColor)
        images.forEach(Consumer { i: ImageWithPosition -> canvas.drawBitmap(i.image, i.left, i.top, paint) })
        canvas.save()
        canvas.restore()
        return result
    }
}