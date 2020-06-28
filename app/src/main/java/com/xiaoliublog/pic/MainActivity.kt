package com.xiaoliublog.pic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.xiaoliublog.pic.MainActivityViewModel
import com.xiaoliublog.pic.databinding.ActivityMainBinding
import com.xiaoliublog.pic.ui.MyImageView
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException

class MainActivity : AppCompatActivity() {
    var options: BitmapFactory.Options? = null
    private val disposable = CompositeDisposable()
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding!!

    private var _model: MainActivityViewModel? = null
    private val model
        get() = _model!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        _model = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewmodel = model
        binding.lifecycleOwner = this

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

        options = BitmapFactory.Options()
        options!!.inDensity = DisplayMetrics.DENSITY_400
    }

    fun selectImg(view: View?) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "选择截图"), IMAGE_PICKER)
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