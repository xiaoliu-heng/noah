package com.xiaoliublog.pic

import android.app.Application
import android.content.Intent
import android.content.res.Resources
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
    private val noahOne = NoahOne(getApplication())
    private val noahTwo = NoahTwo(getApplication())
    private var aspect = 1.86

    val loading = MutableLiveData<Boolean>(true)
    private val _result = MutableLiveData<Bitmap>()
    private val content = MutableLiveData<Bitmap?>()
    private val _bgColor = MutableLiveData(BackgroundColor.White)
    private val fgColor = MutableLiveData<PhoneColor>(PhoneColor.White)
    private val options = BitmapFactory.Options()

    private val _phone = MutableLiveData<Phone>(noahTwo)

    val bg = _bgColor
    val fg = fgColor;
    val result: LiveData<Bitmap> = _result
    val isDark: LiveData<Boolean>
        get() = MutableLiveData<Boolean>(_bgColor.value!! == BackgroundColor.Black)
    val phone: LiveData<Phone> = _phone

    init {
        options.inDensity = DisplayMetrics.DENSITY_400
        val resources: Resources = getApplication<Application>().resources
        val dm: DisplayMetrics = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        Log.d(TAG, "device: width=${width},height=${height},${height / width.toFloat()}")
        aspect = (height / width.toFloat()).toDouble()
        reRender()
    }

    fun saveImg() {
        loading.value = true
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
                .doFinally { loading.value = false }
                .subscribe()
    }

    fun build(export: Boolean? = false): Bitmap {
        val currentPhone = _phone.value!!
        val isTwo: Boolean = currentPhone is NoahTwo
        val paddingX = if (isTwo) 0 else 957
        val paddingY = if (isTwo) 0 else 1400
        val imageComposeBuilder = ImageCombiner()
        val bgColor = bg.value!!
        var frameColor = fgColor.value!!
        if (isTwo) {
            if (isDark.value!!) {
                if (frameColor == PhoneColor.White) frameColor = PhoneColor.WhiteOnBlack
            } else if (frameColor == PhoneColor.Black) {
                frameColor = PhoneColor.BlackOnWhite
            }
        }
        val frameWidth = (currentPhone.width - paddingX).toInt()
        val frameHeight = (currentPhone.height - paddingY).toInt()

        imageComposeBuilder.bgColor = bgColor
        imageComposeBuilder.width = frameWidth + paddingX
        imageComposeBuilder.height = frameHeight + paddingY

        val images = ArrayList<ImageWithPosition>()

        val oldBarHeight = 65
        val newBarHeight = 125
        if (content.value != null) {

            if (!isTwo) currentPhone.top = frameHeight * currentPhone.topOfHeight
            val contentLeft = paddingX / 2f + currentPhone.left
            val contentTop = paddingY / 2f + currentPhone.top
            val contentWidth = frameWidth - currentPhone.left.toInt() * 2
            val contentHeight = frameHeight - currentPhone.top.toInt() * 2

            if (isTwo) {
                Log.d(TAG, "build: Two")
                val content = Bitmap.createScaledBitmap(content.value!!, contentWidth, contentHeight, true)
                images.add(ImageWithPosition(contentLeft, contentTop, content))
            } else {
                Log.d(TAG, "build: One")
                val twoClip = Bitmap.createBitmap(Bitmap.createScaledBitmap(content.value!!, contentWidth, contentHeight, true), 0, oldBarHeight, contentWidth, contentHeight - oldBarHeight)
                val content = Bitmap.createScaledBitmap(twoClip, contentWidth, contentHeight - newBarHeight * 2, true)
                images.add(ImageWithPosition(contentLeft, contentTop + newBarHeight, content))
            }

            if (!isTwo) {
                val color = content.value!!.getPixel(content.value!!.width / 2, 1)
                val barBg = Bitmap.createBitmap(contentWidth, newBarHeight, Bitmap.Config.ARGB_8888)
                if (computeContrastBetweenColors(color) < 3f) {
                    val blackBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_black, options)
                    barBg.eraseColor(BackgroundColor.White)
                    images.add(ImageWithPosition(contentLeft, contentTop, barBg))
                    images.add(ImageWithPosition(contentLeft, contentTop + contentHeight - newBarHeight, barBg))
                    images.add(ImageWithPosition(contentLeft + 5, contentTop + 5, Bitmap.createScaledBitmap(blackBar, contentWidth - 5, contentHeight - 5, true)))
                } else {
                    val whiteBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_white, options)
                    barBg.eraseColor(BackgroundColor.Black)
                    images.add(ImageWithPosition(contentLeft, contentTop, barBg))
                    images.add(ImageWithPosition(contentLeft, contentTop + contentHeight - newBarHeight, barBg))
                    images.add(ImageWithPosition(contentLeft + 5, contentTop + 5, Bitmap.createScaledBitmap(whiteBar, contentWidth - 5, contentHeight - 5, true)))
                }
            }
        }

        val frame = currentPhone.load(frameColor)
        if (frame != null) {
            images.add(ImageWithPosition(paddingX / 2f, paddingY / 2f, Bitmap.createScaledBitmap(frame, frameWidth, frameHeight, true)))
        }
        imageComposeBuilder.images = images
        return imageComposeBuilder.combine()
    }

    fun reRender() {
        Log.d(TAG, "reRender: ")
        loading.postValue(true)
        kotlin.run {
            _result.postValue(build(false))
        }
        loading.postValue(false)
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
        content.value = bitmap
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