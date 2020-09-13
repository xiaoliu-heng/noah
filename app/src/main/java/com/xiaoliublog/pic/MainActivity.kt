package com.xiaoliublog.pic

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xiaoliublog.pic.databinding.ActivityMainBinding
import com.xiaoliublog.pic.ui.MyImageView
import com.yuyashuai.frameanimation.FrameAnimation
import com.yuyashuai.frameanimation.FrameAnimation.RepeatMode
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding!!

    private var _model: MainActivityViewModel? = null
    private val model
        get() = _model!!

    private var canvas: ImageView? = null
    private lateinit var splashView: TextureView
    private lateinit var splashFirst: ImageView
    private lateinit var aboutPage: LinearLayout
    private var frameAnimation: FrameAnimation? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)

        _model = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        canvas = findViewById(R.id.canvas)
        sharedPreferences = getSharedPreferences("com.xiaoliublog.pic", MODE_PRIVATE)

        splashView = findViewById(R.id.splash)
        splashFirst = findViewById(R.id.splash_first)
        aboutPage = findViewById(R.id.about)
        frameAnimation = FrameAnimation(splashView)
        frameAnimation!!.setRepeatMode(RepeatMode.ONCE)
        frameAnimation!!.setScaleType(FrameAnimation.ScaleType.FIT_CENTER)
        frameAnimation!!.freezeLastFrame(true)
        frameAnimation!!.setSupportInBitmap(true)

        fun hideSplash() {
            splashView.animate()
                    .alpha(0f)
                    .withEndAction {
                        frameAnimation!!.stopAnimation()
                        splashView.visibility = View.GONE
                    }
                    .setDuration(500)
                    .start()
        }

        val firstRun = sharedPreferences.getBoolean("firstRun", true)

        Log.d(TAG, "onCreate: firstRun=$firstRun")

        if (firstRun) {
            splashFirst.visibility = View.VISIBLE
        }

        splashView.setOnClickListener {
            if (firstRun) {
                frameAnimation!!.playAnimationFromAssets("splash2")
                Timer("hide splash first", false)
                        .schedule(timerTask { runOnUiThread { splashFirst.visibility = View.GONE } }, 200)
                sharedPreferences.edit {
                    putBoolean("firstRun", false)
                    commit()
                }
            } else {
                hideSplash()
            }
        }

        frameAnimation!!.setAnimationListener(object : FrameAnimation.FrameAnimationListener {
            override fun onAnimationEnd() {
                hideSplash()
            }

            override fun onAnimationStart() {}

            override fun onProgress(progress: Float, frameIndex: Int, totalFrames: Int) {}

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

    fun toggleAbout(view: View?) {
        if (aboutPage.alpha == 0f) aboutPage.visibility = View.VISIBLE
        aboutPage.animate()
                .alpha(if (aboutPage.alpha == .9f) 0f else .9f)
                .withEndAction { if (aboutPage.alpha == 0f) aboutPage.visibility = View.GONE }
                .start()
        view!!.animate()
                .rotation(if (view.rotation == 90f) 0f else 90f)
                .start()
    }

    @AfterPermissionGranted(Storage_Code)
    fun saveImg(view: View?) {
        val perms = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            model.loading.value = true
            Single.create { emitter: SingleEmitter<String?> ->
                val path = Environment.getExternalStorageDirectory().toString()
                val name = "Screenshot_" + Date().time + "_套壳截屏.png"
                val filename = "$path/Pictures/Screenshots/$name"
                try {
                    FileOutputStream(filename).use { out ->
                        model.build(true).compress(Bitmap.CompressFormat.PNG, 100, out)
                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$filename")))
                        emitter.onSuccess("保存成功")
                    }
                } catch (e: IOException) {
                    emitter.onError(e)
                }
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess { ret -> Toast.makeText(this, ret, Toast.LENGTH_SHORT).show() }
                    .doOnError { throwable: Throwable? -> Toast.makeText(this, throwable?.message, Toast.LENGTH_SHORT).show() }
                    .doFinally { model.loading.value = false }
                    .subscribe()
        } else {
            EasyPermissions.requestPermissions(this, "需要储存权限", Storage_Code, *perms)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!sharedPreferences.getBoolean("firstRun", true)) {
            frameAnimation!!.playAnimationFromAssets("splash")
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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