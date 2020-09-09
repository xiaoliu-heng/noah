package com.xiaoliublog.pic

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xiaoliublog.pic.databinding.ActivityMainBinding
import com.xiaoliublog.pic.ui.MyImageView
import com.yuyashuai.frameanimation.FrameAnimation
import com.yuyashuai.frameanimation.FrameAnimation.RepeatMode
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding!!

    private var _model: MainActivityViewModel? = null
    private val model
        get() = _model!!

    private var canvas: ImageView? = null
    var frameAnimation: FrameAnimation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _model = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        canvas = findViewById(R.id.canvas)

        val textureView: SurfaceView = findViewById(R.id.splash)
        val splashBg: LinearLayout = findViewById(R.id.splash_bg)
        splashBg.setBackgroundColor(Color.BLACK)
        splashBg.alpha = 1f
        frameAnimation = FrameAnimation(textureView)
        frameAnimation!!.setRepeatMode(RepeatMode.ONCE)
        frameAnimation!!.setScaleType(FrameAnimation.ScaleType.FIT_CENTER)

        fun hideSplash() {
            textureView.visibility = View.GONE
            val colorAnimator: ValueAnimator = ValueAnimator.ofArgb(Color.BLACK, BackgroundColor.White).apply {
                addUpdateListener { animation ->
                    splashBg.setBackgroundColor(animation.animatedValue as Int)
                }
            }
            val alphaAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
                addUpdateListener { animation ->
                    splashBg.alpha = animation.animatedValue as Float
                }
            }
            AnimatorSet().apply {
                play(colorAnimator)
                play(alphaAnimator)
                duration = 1000
                doOnEnd { splashBg.visibility = View.GONE }
                start()
            }
        }

        val handler = Handler()
        val runnable = Runnable { hideSplash() }
        handler.postDelayed(runnable, 6006)

        textureView.setOnClickListener(View.OnClickListener { v: View? ->
            handler.removeCallbacks(runnable)
            hideSplash()
        })

        binding.viewmodel = model
        binding.lifecycleOwner = this

        model.bg.observe(this, Observer { t -> model.reRender() })
        model.result.observe(this, Observer { res -> canvas?.setImageBitmap(res) })

        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if (type.startsWith("image/")) {
                Log.d(TAG, "收到图片文件")
                val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
                imageUri.let { setBitmapFromUri(it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        frameAnimation!!.playAnimationFromAssets("splash")
    }

    override fun onPause() {
        super.onPause()
        frameAnimation!!.stopAnimation()
    }

    fun selectImg(view: View?) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "选择截图"), IMAGE_PICKER)
    }

    fun shareImg(view: View?) {
        try {
            model.loading.value = true
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream = FileOutputStream("$cachePath/image.png") // overwrites this image every time
            model.build(true).compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imagePath = File(cacheDir, "images")
            val newFile = File(imagePath, "image.png")
            val contentUri = FileProvider.getUriForFile(this, "com.xiaoliublog.pic.fileprovider", newFile)

            if (contentUri != null) {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                startActivity(Intent.createChooser(shareIntent, "选择要分享的APP"))
            }
            stream.close()
            model.loading.value = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun setBitmapFromUri(uri: Uri) {
        try {
            val bitmap = getBitmapFromUri(uri)
            model.setTwo(bitmap)
        } catch (e: IOException) {
            Log.d(TAG, "setBitmapFromUri: read image from uri failed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICKER && data != null) {
            setBitmapFromUri(data.data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        _binding = null
        frameAnimation!!.stopAnimation()
    }

    companion object {
        private const val IMAGE_PICKER = 99
        private const val TAG = "hyl"

        @JvmStatic
        @BindingAdapter("bitmap")
        fun setBitmap(imageView: MyImageView, src: Bitmap?) {
            imageView.setImageBitmap(src)
        }

        @JvmStatic
        @BindingAdapter("background_color")
        fun setBgColor(view: View, color: LiveData<Int?>) {
            view.setBackgroundColor(color.value!!)
        }
    }
}