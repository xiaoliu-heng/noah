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
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xiaoliublog.pic.model.NoahOne
import com.xiaoliublog.pic.model.NoahTwo
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
    private val noahOne = NoahOne(getApplication())
    private val noahTwo = NoahTwo(getApplication())

    var _width = MutableLiveData<Int>(2700)
    var _height = MutableLiveData<Int>(5175)

    var frameWidth = 1635
    var frameHeight = 2883

    private val _result = MutableLiveData<Bitmap>()
    private val two = MutableLiveData<Bitmap?>()
    private val _bgColor = MutableLiveData(BackgroundColor.Black)
    private val fgColor = MutableLiveData<PhoneColor>(PhoneColor.Black)
    private val options = BitmapFactory.Options()

    private val _phone = MutableLiveData<Phone>(noahOne)

    val bg = _bgColor
    val result: LiveData<Bitmap> = _result
    val isDark: Boolean = _bgColor.value?.equals(BackgroundColor.Black) ?: true
    val phone: LiveData<Phone> = _phone

    init {
        options.inDensity = DisplayMetrics.DENSITY_400
        _t3 = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.pro2s_noah1_black, options)
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
        val currentPhone = _phone.value!!
        val isTwo: Boolean = currentPhone is NoahTwo
        val padding_h = if (isTwo) 0 else 600
        val padding_v = if (isTwo) 0 else 800
        val padding_top = if (export == true) padding_v / 2f else padding_v / 4f
        val imageComposeBuilder = ImageCombiner()
        val bgColor = bg.value!!
        var frameColor = fgColor.value!!
        if (isTwo) {
            if (bgColor == BackgroundColor.Black) {
                if (frameColor == PhoneColor.White) frameColor = PhoneColor.WhiteOnBlack
            }
            if (bgColor == BackgroundColor.White) {
                if (frameColor == PhoneColor.Black) frameColor = PhoneColor.BlackOnWhite
            }
        }
        val frame = currentPhone.colors.getOrDefault(frameColor, _t3)
        frameHeight = currentPhone.height.toInt()
        frameWidth = currentPhone.width.toInt()
        imageComposeBuilder.bgColor = bgColor
        imageComposeBuilder.width = frameWidth + padding_h
        imageComposeBuilder.height = frameHeight + padding_v
        Log.d(TAG, "reRender: canvas width=${imageComposeBuilder.width},height=${imageComposeBuilder.height}. image width=${frameWidth},height=${frameHeight}")

        val images = ArrayList<ImageWithPosition>()

        val oldBarHeight = 55
        val newBarHeight = 105
        if (two.value != null) {
            Log.d(TAG, "reRender: two width=${two.value!!.width},height=${two.value!!.height}")
            val twoLeft = padding_h / 2f + currentPhone.left
            val twoTop = padding_top + currentPhone.top
            val twoWidth = frameWidth - currentPhone.left.toInt() * 2
            val twoHeight = frameHeight - currentPhone.top.toInt() * 2

            if (isTwo) {
                val content = Bitmap.createScaledBitmap(two.value!!, twoWidth, twoHeight, true)
                images.add(ImageWithPosition(twoLeft, twoTop, content))
            } else {
                val twoClip = Bitmap.createBitmap(Bitmap.createScaledBitmap(two.value!!, twoWidth, twoHeight, true), 0, oldBarHeight, twoWidth, twoHeight - oldBarHeight)
                val content = Bitmap.createScaledBitmap(twoClip, twoWidth, twoHeight - newBarHeight * 2, true)
                images.add(ImageWithPosition(twoLeft, twoTop + newBarHeight, content))
            }


            if (!isTwo) {
                val color = two.value!!.getPixel(two.value!!.width / 2, 1)
                val barBg = Bitmap.createBitmap(twoWidth, newBarHeight, Bitmap.Config.ARGB_8888)
                if (computeContrastBetweenColors(color) < 3f) {
                    val blackBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_black, options)
                    barBg.eraseColor(BackgroundColor.White)
                    images.add(ImageWithPosition(twoLeft, twoTop, barBg))
                    images.add(ImageWithPosition(twoLeft, twoTop + twoHeight - newBarHeight, barBg))
                    images.add(ImageWithPosition(twoLeft, twoTop + 5, Bitmap.createScaledBitmap(blackBar, twoWidth, twoHeight - 5, true)))
                } else {
                    val whiteBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_white, options)
                    barBg.eraseColor(BackgroundColor.Black)
                    images.add(ImageWithPosition(twoLeft, twoTop, barBg))
                    images.add(ImageWithPosition(twoLeft, twoTop + twoHeight - newBarHeight, barBg))
                    images.add(ImageWithPosition(twoLeft, twoTop + 5, Bitmap.createScaledBitmap(whiteBar, twoWidth, twoHeight - 5, true)))
                }
            }
        }
        images.add(ImageWithPosition(padding_h / 2f, padding_top, Bitmap.createScaledBitmap(frame, frameWidth, frameHeight, true)))
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

    private fun animation(view: View) {
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

    fun switchNoah1(view: View) {
        animation(view)
        _phone.value = noahOne
        reRender()
    }

    fun switchNoah2(view: View) {
        animation(view)
        _phone.value = noahTwo
        reRender()
    }

    fun showAbout(view: View) {
        view.animate()
                .rotation(if (view.rotation == 90f) 0f else 90f)
                .start()
        Log.d(TAG, "showAbout: ")
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