package com.xiaoliublog.pic

import android.app.Application
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xiaoliublog.pic.model.NoahOne
import com.xiaoliublog.pic.model.NoahTwo
import com.xiaoliublog.pic.model.Phone
import com.xiaoliublog.pic.model.PhoneColor
import com.xiaoliublog.pic.utils.ImageCombiner
import com.xiaoliublog.pic.utils.ImageWithPosition
import com.xiaoliublog.pic.utils.animation


class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val noahOne = NoahOne(getApplication())
    private val noahTwo = NoahTwo(getApplication())
    private var aspect = 1.86f

    val loading = MutableLiveData<Boolean>(true)
    private val _result = MutableLiveData<Bitmap>()
    private val content = MutableLiveData<Bitmap?>()
    val bg = MutableLiveData(BackgroundColor.White)
    val isDark = MutableLiveData<Boolean>(false)
    val fg = MutableLiveData<PhoneColor>(PhoneColor.White)
    private val options = BitmapFactory.Options()

    private val _phone = MutableLiveData<Phone>(noahTwo)

    val result: LiveData<Bitmap> = _result
    val phone: LiveData<Phone> = _phone

    init {
        options.inDensity = DisplayMetrics.DENSITY_400
        val resources: Resources = getApplication<Application>().resources
        val dm: DisplayMetrics = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        Log.d(TAG, "device: width=${width},height=${height},${height / width.toFloat()}")
        aspect = height / width.toFloat()
        reRender()
    }

    fun build(export: Boolean? = false): Bitmap {
        loading.postValue(true)
        val currentPhone = _phone.value!!
        val isTwo: Boolean = currentPhone is NoahTwo
        val paddingX = if (isTwo) 0 else 957
        val paddingY = if (isTwo) 0 else 1440
        val imageComposeBuilder = ImageCombiner()
        val bgColor = this.bg.value!!
        var frameColor = this.fg.value!!
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

        val oldBarHeight = 63
        val newBarHeight = 125
        if (content.value != null) {

            val img = content.value!!

            if (!isTwo) currentPhone.top = frameHeight * currentPhone.topOfHeight
            val contentLeft = paddingX / 2f + currentPhone.left
            var contentTop = paddingY / 2f + currentPhone.top
            val contentWidth = frameWidth - currentPhone.left.toInt() * 2
            val contentHeight = frameHeight - currentPhone.top.toInt() * 2

            if (isTwo) {
                Log.d(TAG, "build: Two")
                val content = Bitmap.createScaledBitmap(content.value!!, contentWidth, contentHeight - 150, true)
                images.add(ImageWithPosition(contentLeft, contentTop + 3, content))
            } else {
                Log.d(TAG, "build: One")
                val twoClip = Bitmap.createBitmap(Bitmap.createScaledBitmap(content.value!!, contentWidth, contentHeight, true), 0, oldBarHeight, contentWidth, contentHeight - oldBarHeight)
                val content = Bitmap.createScaledBitmap(twoClip, contentWidth, contentHeight - newBarHeight * 2, true)
                images.add(ImageWithPosition(contentLeft, contentTop + newBarHeight, content))
            }

            contentTop += 3f
            val topColor = img.getPixel(4, 4)
            val bottomColor = img.getPixel(4, img.height - 4)

            val topBg = Bitmap.createBitmap(contentWidth, 10, Bitmap.Config.ARGB_8888)
            topBg.eraseColor(topColor)
            images.add(ImageWithPosition(contentLeft, contentTop - 10, topBg))

            if (!isTwo) {
                val statusBg = Bitmap.createBitmap(contentWidth, newBarHeight, Bitmap.Config.ARGB_8888)
                statusBg.eraseColor(topColor)
                val barBg = Bitmap.createBitmap(contentWidth, newBarHeight + 10, Bitmap.Config.ARGB_8888)
                barBg.eraseColor(bottomColor)

                val barFg = if (computeContrastBetweenColors(topColor) < 3f) {
                    BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_black, options)
                } else {
                    BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_white, options)
                }

                images.add(ImageWithPosition(contentLeft, contentTop, barBg))
                images.add(ImageWithPosition(contentLeft, contentTop + contentHeight - newBarHeight - 3f, barBg))
                images.add(ImageWithPosition(contentLeft + 5, contentTop + 5, Bitmap.createScaledBitmap(barFg, contentWidth - 5, contentHeight - 5, true)))
            }

            if (isTwo) {
                val barBg = Bitmap.createBitmap(contentWidth, 150, Bitmap.Config.ARGB_8888)
                barBg.eraseColor(bottomColor)
                images.add(ImageWithPosition(contentLeft, contentTop + contentHeight - 150, barBg))

                val bar = if (computeContrastBetweenColors(bottomColor) < 3f) {
                    BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.menu_bar_black, options)
                } else {
                    BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.menu_bar_white, options)
                }
                images.add(ImageWithPosition(contentLeft, contentTop + contentHeight - 150, Bitmap.createScaledBitmap(bar, contentWidth, 147, true)))
            }
        }

        val frame = currentPhone.load(frameColor)
        if (frame != null) {
            images.add(ImageWithPosition(paddingX / 2f, paddingY / 2f, Bitmap.createScaledBitmap(frame, frameWidth, frameHeight, true)))
        }
        imageComposeBuilder.images = images
        loading.postValue(false)

        return imageComposeBuilder.combine()
    }

    fun reRender() {
        Log.d(TAG, "reRender: ")
        loading.postValue(true)
        _result.postValue(build(false))
        loading.postValue(false)
    }

    fun setBgColor(color: Int) {
        Log.d(TAG, "setBgColor: $color")
        if (color == BackgroundColor.Black) {
            isDark.postValue(true)
        } else {
            isDark.postValue(false)
        }
        this.bg.postValue(color)
        reRender()
    }

    fun setFgColor(color: PhoneColor) {
        Log.d(TAG, "setFgColor: $color")
        this.fg.value = color
        reRender()
    }

    fun setTwo(bitmap: Bitmap?) {
        content.value = bitmap
        reRender()
    }

    fun switchNoah1(view: View) {
        animation(view)
        if (this.fg.value != PhoneColor.White || this.fg.value != PhoneColor.Black) {
            this.fg.value = PhoneColor.White
        }
        _phone.value = noahOne
        reRender()
    }

    fun switchNoah2(view: View) {
        animation(view)
        if (this.fg.value != PhoneColor.White || this.fg.value != PhoneColor.Black) {
            this.fg.value = PhoneColor.White
        }
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