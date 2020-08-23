package com.xiaoliublog.pic

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xiaoliublog.pic.model.NoahPro2s
import com.xiaoliublog.pic.model.Phone
import com.xiaoliublog.pic.model.PhoneColor
import com.xiaoliublog.pic.utils.ImageCombiner
import com.xiaoliublog.pic.utils.ImageWithPosition
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val _t3: Bitmap
    private val phone: Phone
    var _width = MutableLiveData<Int>(2700)

    var _height = MutableLiveData<Int>(5175)

    var frameWidth = 1635
    var frameHeight = 2883

    private val _result = MutableLiveData<Bitmap>()
    private val two = MutableLiveData<Bitmap?>()
    private val _bgColor = MutableLiveData(BackgroundColor.Black)
    private val fgColor = MutableLiveData<PhoneColor>(PhoneColor.Black)
    private val options = BitmapFactory.Options()
    private val blackBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_black, options)
    private val whiteBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_white, options)

    val bg = _bgColor
    val result: LiveData<Bitmap> = _result
    val isDark: Boolean = _bgColor.value?.equals(BackgroundColor.Black) ?: true

    init {
        options.inDensity = DisplayMetrics.DENSITY_400
        _t3 = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.pro2s_noah1_black, options)
        phone = NoahPro2s(getApplication())
        reRender()
    }

    fun saveImg() {
        Single.create { emitter: SingleEmitter<String?> ->
            val path = Environment.getExternalStorageDirectory().toString()
            val name = "Screenshot_" + Date().time + "_套壳截屏.png"
            val filename = "$path/Pictures/Screenshots/$name"
            try {
                FileOutputStream(filename).use { out ->
                    build(true).compress(Bitmap.CompressFormat.PNG, 100, out)
                    getApplication<Application>().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$filename")))
                    emitter.onSuccess("保存成功")
                }
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { ret -> Toast.makeText(getApplication(), ret, Toast.LENGTH_SHORT).show() }
                .doOnError { throwable: Throwable? -> Toast.makeText(getApplication(), throwable?.message, Toast.LENGTH_SHORT).show() }
                .subscribe()
    }

    fun build(export: Boolean? = false): Bitmap {
        val padding_h = 400
        val padding_v = 600
        val padding_top = if (export == true) padding_v / 2f else padding_v / 4f
        val imageComposeBuilder = ImageCombiner()
        val current = phone.colors.getOrDefault(fgColor.value!!, _t3)
        frameHeight = current.height / 4
        frameWidth = current.width / 4
        imageComposeBuilder.bgColor = (if (_bgColor.value == null) BackgroundColor.Transparent else _bgColor.value)!!
        imageComposeBuilder.width = frameWidth + padding_h
        imageComposeBuilder.height = frameHeight + padding_v
        Log.d(TAG, "reRender: canvas width=${imageComposeBuilder.width},height=${imageComposeBuilder.height}. image width=${frameWidth},height=${frameHeight}")

        val images = ArrayList<ImageWithPosition>()
        if (two.value != null) {
            Log.d(TAG, "reRender: two width=${two.value!!.width},height=${two.value!!.height}")
            val two_left = phone.left + padding_h / 2f - 45
            val two_top = phone.top + padding_top - 40
            val two_width = frameWidth - phone.left.toInt() / 2 * 2 - 15
            val two_height = frameHeight - phone.top.toInt() / 2 * 2 - 5
            images.add(ImageWithPosition(two_left, two_top,
                    Bitmap.createScaledBitmap(two.value!!, two_width, two_height, true)))
            val color = two.value!!.getPixel(two.value!!.width / 2, 1)
            val bar_bg = Bitmap.createBitmap(two_width, 125, Bitmap.Config.ARGB_8888);
            if (computeContrastBetweenColors(color) < 3f) {
                bar_bg.eraseColor(BackgroundColor.White)
                images.add(ImageWithPosition(two_left, two_top, bar_bg))
                images.add(ImageWithPosition(two_left, two_top, Bitmap.createScaledBitmap(blackBar, two_width, two_height, true)))
            } else {
                bar_bg.eraseColor(BackgroundColor.Black)
                images.add(ImageWithPosition(two_left, two_top, bar_bg))
                images.add(ImageWithPosition(two_left, two_top, Bitmap.createScaledBitmap(whiteBar, two_width, two_height, true)))
            }
        }
        images.add(ImageWithPosition(padding_h / 2f, padding_top, Bitmap.createScaledBitmap(current, frameWidth, frameHeight, true)))
        imageComposeBuilder.images = images
        val res = imageComposeBuilder.combine()
        Log.d(TAG, "reRender: result: width:${res.width},height:${res.height}")
        return res
    }

    fun reRender() {
        _result.postValue(build(false))
    }

    fun setBgColor(color: Int) {
        Log.d(TAG, "setBgColor: $color")
        _bgColor.value = color
        reRender()
    }

    fun setFgColor(color: PhoneColor) {
        Log.d(TAG, "setFgColor: $color")
        fgColor.value = color
        reRender()
    }

    fun setTwo(bitmap: Bitmap?) {
        two.value = bitmap
        reRender()
    }

    companion object {
        private const val TAG = "hyl"
        private fun computeContrastBetweenColors(bg: Int): Float {
            var bgR = Color.red(bg) / 255f
            var bgG = Color.green(bg) / 255f
            var bgB = Color.blue(bg) / 255f
            bgR = if (bgR < 0.03928f) bgR / 12.92f else Math.pow((bgR + 0.055f) / 1.055f.toDouble(), 2.4).toFloat()
            bgG = if (bgG < 0.03928f) bgG / 12.92f else Math.pow((bgG + 0.055f) / 1.055f.toDouble(), 2.4).toFloat()
            bgB = if (bgB < 0.03928f) bgB / 12.92f else Math.pow((bgB + 0.055f) / 1.055f.toDouble(), 2.4).toFloat()
            val bgL = 0.2126f * bgR + 0.7152f * bgG + 0.0722f * bgB
            var fgR = Color.red(Color.WHITE) / 255f
            var fgG = Color.green(Color.WHITE) / 255f
            var fgB = Color.blue(Color.WHITE) / 255f
            fgR = if (fgR < 0.03928f) fgR / 12.92f else Math.pow((fgR + 0.055f) / 1.055f.toDouble(), 2.4).toFloat()
            fgG = if (fgG < 0.03928f) fgG / 12.92f else Math.pow((fgG + 0.055f) / 1.055f.toDouble(), 2.4).toFloat()
            fgB = if (fgB < 0.03928f) fgB / 12.92f else Math.pow((fgB + 0.055f) / 1.055f.toDouble(), 2.4).toFloat()
            val fgL = 0.2126f * fgR + 0.7152f * fgG + 0.0722f * fgB
            return Math.abs((fgL + 0.05f) / (bgL + 0.05f))
        }
    }
}