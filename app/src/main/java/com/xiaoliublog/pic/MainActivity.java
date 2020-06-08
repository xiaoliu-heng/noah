package com.xiaoliublog.pic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import lombok.val;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_PICKER = 99;
    private static final String TAG = "hyl";
    MainActivityViewModel model;
    CoordinatorLayout coordinatorLayout;
    Bitmap tnt;
    ImageView iv;
    ProgressBar saving;
    FloatingActionButton fab_save;
    ExtendedFloatingActionButton efab_transparent, efab_white, efab_black;
    BitmapFactory.Options options;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        model = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Log.d(TAG, "收到图片文件");
                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    setBitmapFromUri(imageUri);
                }
            }
        }

        init();
        initBg();

        model.result.observe(this, r -> {
            Log.d(TAG, "onCreate: set image from viewModel");

            if (r != null) {
                iv.setImageBitmap(r);
            }
        });
    }

    private void init() {

        iv = findViewById(R.id.canvas);
        saving = findViewById(R.id.saving);
        coordinatorLayout = findViewById(R.id.bg);

        saving.setVisibility(View.INVISIBLE);
        fab_save = findViewById(R.id.fab_save);
        efab_transparent = findViewById(R.id.efab_transparent);
        efab_black = findViewById(R.id.efab_black);
        efab_white = findViewById(R.id.efab_white);

        iv.setOnClickListener(this);
        fab_save.setOnClickListener(this);
        efab_transparent.setOnClickListener(this);
        efab_black.setOnClickListener(this);
        efab_white.setOnClickListener(this);

        options = new BitmapFactory.Options();
        options.inDensity = DisplayMetrics.DENSITY_400;

        Single.create((SingleOnSubscribe<String>) emitter -> {
            tnt = BitmapFactory.decodeResource(getResources(), R.drawable.tnt_transparent, options);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void selectImg(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择截图"), IMAGE_PICKER);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void setBitmapFromUri(Uri uri) {
        try {
            Bitmap bitmap = getBitmapFromUri(uri);
            model.setTwo(bitmap);
        } catch (IOException e) {
            Log.d(TAG, "setBitmapFromUri: read image from uri failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER && data != null) {
            setBitmapFromUri(data.getData());
        }
    }

    public void saveImg() {
        fab_save.setVisibility(View.INVISIBLE);
        saving.setVisibility(View.VISIBLE);
        Single.create(emitter -> {
            String path = Environment.getExternalStorageDirectory().toString();
            String name = "Screenshot_" + new Date().getTime() + "_套壳截屏.png";
            String filename = path + "/Pictures/Screenshots/" + name;
            try (FileOutputStream out = new FileOutputStream(filename)) {
                if (model.result.getValue() != null) {
                    model.result.getValue().compress(Bitmap.CompressFormat.PNG, 100, out);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filename)));
                    emitter.onSuccess("成功");
                } else {
                    emitter.onError(new Exception("no result"));
                }
            } catch (IOException e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(ret -> {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                })
                .doOnError(throwable -> {
                    Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                })
                .doFinally(() -> {
                    saving.setVisibility(View.INVISIBLE);
                    fab_save.setVisibility(View.VISIBLE);
                })
                .subscribe();
    }

    public void setBgColor(int color){
        model.setBgColor(color);
    }

    public void initBg() {
        model.bgColor.observe(this, c -> {
            if (c == BackgroundColor.Transparent) {
                efab_transparent.setIcon(getDrawable(R.drawable.icon_check));
                efab_white.setIcon(getDrawable(R.drawable.icon_unchecked));
                efab_black.setIcon(getDrawable(R.drawable.icon_unchecked));
            } else if (c == BackgroundColor.Black) {
                efab_transparent.setIcon(getDrawable(R.drawable.icon_unchecked));
                efab_white.setIcon(getDrawable(R.drawable.icon_unchecked));
                efab_black.setIcon(getDrawable(R.drawable.icon_check));
            } else if (c == BackgroundColor.White) {
                efab_transparent.setIcon(getDrawable(R.drawable.icon_unchecked));
                efab_white.setIcon(getDrawable(R.drawable.icon_check));
                efab_black.setIcon(getDrawable(R.drawable.icon_unchecked));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.canvas:
                selectImg(v);
                break;
            case R.id.fab_save:
                saveImg();
                break;
            case R.id.efab_transparent:
                setBgColor(BackgroundColor.Transparent);
                break;
            case R.id.efab_white:
                setBgColor(BackgroundColor.White);
                break;
            case R.id.efab_black:
                setBgColor(BackgroundColor.Black);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
