package com.xiaoliublog.pic

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xiaoliublog.pic.databinding.ActivityMainBinding
import com.xiaoliublog.pic.utils.animation
import com.xiaoliublog.pic.utils.getDeviceName
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

    private lateinit var canvas: ImageView
    private lateinit var splashView: TextureView
    private lateinit var splashFirst: ImageView
    private lateinit var aboutPage: LinearLayout
    private lateinit var frameAnimation: FrameAnimation
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height: Int = displayMetrics.heightPixels
        val width: Int = displayMetrics.widthPixels

        _model = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setDevice()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        canvas = findViewById(R.id.canvas)
        sharedPreferences = getSharedPreferences("com.xiaoliublog.pic", MODE_PRIVATE)

        splashView = findViewById(R.id.splash)
        splashFirst = findViewById(R.id.splash_first)
        aboutPage = findViewById(R.id.about)
        frameAnimation = FrameAnimation(splashView)
        frameAnimation.setRepeatMode(RepeatMode.ONCE)
        frameAnimation.freezeLastFrame(true)

        frameAnimation.setScaleType(FrameAnimation.ScaleType.FIT_XY)
        frameAnimation.setFrameInterval(33)

        fun hideSplash() {
            splashView.animate()
                    .alpha(0f)
                    .withEndAction {
                        frameAnimation.stopAnimation()
                        splashView.visibility = View.GONE
                        splashFirst.visibility = View.GONE
                    }
                    .setDuration(500)
                    .start()
        }

        if (sharedPreferences.getBoolean("firstRun", true)) {
            splashFirst.setImageResource(R.drawable.splash_first)
        }

        splashView.setOnClickListener {
            if (sharedPreferences.getBoolean("firstRun", true)) {
                frameAnimation.playAnimationFromAssets("splash2")
                Timer("hide splash first", false)
                        .schedule(timerTask { runOnUiThread { splashFirst.setImageResource(R.drawable.bg_white) } }, 200)
                sharedPreferences.edit {
                    putBoolean("firstRun", false)
                    commit()
                }
            } else {
                hideSplash()
            }
        }

        frameAnimation.setAnimationListener(object : FrameAnimation.FrameAnimationListener {
            override fun onAnimationEnd() {
                Log.d(TAG, "onAnimationEnd: ")
                hideSplash()
            }

            override fun onAnimationStart() {}

            override fun onProgress(progress: Float, frameIndex: Int, totalFrames: Int) {}

        })

        binding.viewmodel = model
        binding.lifecycleOwner = this

        model.bg.observe(this, Observer { t -> model.reRender() })
        model.result.observe(this, Observer { res -> canvas.setImageBitmap(res) })

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
        if (!sharedPreferences.getBoolean("firstRun", true)) {
            frameAnimation.playAnimationFromAssets("splash")
            Timer("hide splash first", false)
                    .schedule(timerTask { runOnUiThread { splashFirst.visibility = View.GONE } }, 200)
        }
    }

    override fun onPause() {
        super.onPause()
        frameAnimation.stopAnimation()
    }

    fun setDevice() {
        val deviceName = getDeviceName()
        Log.d(TAG, "setDevice: $deviceName")
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

    @AfterPermissionGranted(100)
    fun saveImg(view: View?) {
        animation(view!!)
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
            EasyPermissions.requestPermissions(this, "需要储存权限", 100, *perms)
        }
    }

    fun selectImg(view: View?) {
        animation(view!!)
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "选择截图"), IMAGE_PICKER)
    }

    fun shareImg(view: View?) {
        animation(view!!)
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
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            model.loading.value = false
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                Log.i(TAG, "Uri: $uri")
                val fileName = getFileName(uri)
                if (getDeviceName() === "DT1902A") {
                    setBitmapFromUri(uri)
                } else if (fileName.toLowerCase().contains("screenshot")) {
                    setBitmapFromUri(uri)
                } else {
                    Toast.makeText(this, "(oﾟvﾟ)ノ 只支持屏幕截图哦,你选的是 $fileName", Toast.LENGTH_LONG).show()
                }
            }
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
        frameAnimation.stopAnimation()
    }

    private fun getFileName(uri: Uri): String {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null, null)

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                val displayName: String =
                        it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.i(TAG, "Display Name: $displayName")
                return displayName
            }
        }
        return ""
    }

    companion object {
        private const val IMAGE_PICKER = 99
        private const val TAG = "hyl"

    }
}