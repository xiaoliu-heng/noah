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
import com.xiaoliublog.pic.ui.MyImageView
import com.xiaoliublog.pic.utils.BitmapTransformer
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
    private val transformer = BitmapTransformer(getApplication())

    private val _result = MutableLiveData<Bitmap>()
    private val  two = MutableLiveData<Bitmap?>()
    private val  bgColor = MutableLiveData(BackgroundColor.Transparent)
    private val options = BitmapFactory.Options()
    private val blackBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_black, options)
    private val whiteBar = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.bar_white, options)

    val bg: LiveData<Int?> = bgColor
    val result :LiveData<Bitmap> = _result;

    fun saveImg() {
        Single.create { emitter: SingleEmitter<String?> ->
            val path = Environment.getExternalStorageDirectory().toString()
            val name = "Screenshot_" + Date().time + "_套壳截屏.png"
            val filename = "$path/Pictures/Screenshots/$name"
            try {
                FileOutputStream(filename).use { out ->
                    if (result.value != null) {
                        result.value!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                        getApplication<Application>().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$filename")))
                        emitter.onSuccess("保存成功")
                    } else {
                        emitter.onError(Exception("保存失败"))
                    }
                }
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { ret -> Toast.makeText(getApplication(), ret, Toast.LENGTH_SHORT).show() }
                .doOnError { throwable: Throwable? -> Toast.makeText(getApplication(), throwable?.message, Toast.LENGTH_SHORT).show() }
                .subscribe()
    }

    fun setBg(bg: Int) {
        bgColor.postValue(bg)
    }

    fun reRender() {
        Log.d(TAG, "reRender: " + bgColor.value)
        val imageComposeBuilder = ImageCombiner();
        imageComposeBuilder.bgColor = (if (bgColor.value == null) BackgroundColor.Transparent else bgColor.value)!!
        imageComposeBuilder.width = 1826
        imageComposeBuilder.height = 3252
        val images = ArrayList<ImageWithPosition>();
        images.add(ImageWithPosition(0F, 0F, _t3))
        if (two.value != null) {
            images.add(ImageWithPosition(555F, 297F, Bitmap.createScaledBitmap(two.value!!, 1080, 2155, true)))
            val color = two.value!!.getPixel(two.value!!.width / 2, 1)
            if (computeContrastBetweenColors(color) < 3f) {
                images.add(ImageWithPosition(373F, 426F, blackBar))
            } else {
                images.add(ImageWithPosition(373F, 426F, whiteBar))
            }
        }
        imageComposeBuilder.images = images;
        _result.postValue(imageComposeBuilder.combine())
    }

    fun setBgColor(color: Int) {
        Log.d(TAG, "setBgColor: $color")
        bgColor.value = color
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
        } //    public Bitmap compoundBitmap_t3(Bitmap one, Bitmap two) {
        //        one = one.copy(Bitmap.Config.ARGB_8888, true);
        //        two = two.copy(Bitmap.Config.ARGB_8888, true);
        //        if (two.getWidth() > two.getHeight()) {
        //            Matrix matrix = new Matrix();
        //            matrix.postRotate(-90);
        //            two = Bitmap.createBitmap(two, 0, 0, two.getWidth(), two.getHeight(),
        //                    matrix, true);
        //        }
        //        two = Bitmap.createBitmap(two, 0, 82, two.getWidth(), two.getHeight() - 82);
        //        two = Bitmap.createScaledBitmap(two, 1080, 2155, true);
        //        int color = two.getPixel(two.getWidth() / 2, 1);
        //        Bitmap bar;
        //        if (computeContrastBetweenColors(color) < 3f) {
        //            Log.d(TAG, "compoundBitmap_t3: 白");
        //            bar = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.bar_black, options);
        //        } else {
        //            Log.d(TAG, "compoundBitmap_t3: 黑");
        //            bar = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.bar_white, options);
        //        }
        //        bar = bar.copy(Bitmap.Config.ARGB_8888, true);
        //        Log.d(TAG, "t3:" + one.getWidth() + "x" + one.getHeight());
        //        Log.d(TAG, "two:" + two.getWidth() + "x" + two.getHeight());
        //        Bitmap newBitmap = Bitmap.createBitmap(1826, 3252, Bitmap.Config.ARGB_8888, true);
        //        Canvas canvas = new Canvas(newBitmap);
        //        Paint paint = new Paint();
        //
        ////        Bitmap status_bg_origin = Bitmap.createBitmap(two, 0, 0, two.getWidth(), 2);
        ////        Bitmap status_bg = Bitmap.createScaledBitmap(status_bg_origin, 1080, 124, true);
        //        Bitmap status_bg = Bitmap.createBitmap(1080, 124, Bitmap.Config.ARGB_8888);
        //        status_bg.eraseColor(color);
        ////        Bitmap botton_bg_origin = Bitmap.createBitmap(two,0,two.getHeight()-2,two.getWidth(),2);
        ////        Bitmap bottom_bg = Bitmap.createScaledBitmap(botton_bg_origin,1080,121,true);
        //        Bitmap bottom_bg = Bitmap.createBitmap(1080, 131, Bitmap.Config.ARGB_8888);
        //        bottom_bg.eraseColor(two.getPixel(two.getWidth() / 2, two.getHeight() - 1));
        //
        //        canvas.drawColor(bgColor.getValue());
        //        canvas.drawBitmap(transformer.blur(status_bg), 372, 426, paint);
        //        canvas.drawBitmap(bottom_bg, 372, 2698, paint);
        //        canvas.drawBitmap(two, 372, 550, paint);
        //        canvas.drawBitmap(bar, 373, 426, paint);
        //        canvas.drawBitmap(one, 0, 0, paint);
        //        canvas.save();
        //        canvas.restore();
        //        return newBitmap;
        //    }
    }

    init {
        options.inDensity = DisplayMetrics.DENSITY_400
        _t3 = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.t3_transparent, options)
        _result.postValue(_t3)
    }
}