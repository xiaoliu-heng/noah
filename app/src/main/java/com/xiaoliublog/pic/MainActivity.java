package com.xiaoliublog.pic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.xiaoliublog.pic.utils.BitmapTransformer;
import com.xiaoliublog.pic.utils.PicassoImageLoader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_PICKER = 99;
    private static final String TAG = "hyl";
    CoordinatorLayout coordinatorLayout;
    Bitmap t3, result, t3_two, tnt_two, tnt;
    ImageView iv;
    ProgressBar saving;
    FloatingActionButton fab_switch, fab_save;
    ExtendedFloatingActionButton efab_transparent, efab_white, efab_black;
    BitmapFactory.Options options;
    String current = "t3";
    int currentBgColor = Color.parseColor("#242424");
    int BgBlack = Color.parseColor("#242424");
    int BgWhite = Color.parseColor("#E6E6E6");

    private BitmapTransformer transformer;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        transformer = new BitmapTransformer(this);

        iv = findViewById(R.id.canvas);
        saving = findViewById(R.id.saving);
        coordinatorLayout = findViewById(R.id.bg);

        saving.setVisibility(View.INVISIBLE);
        fab_switch = findViewById(R.id.fab_switch);
        fab_save = findViewById(R.id.fab_save);
        efab_transparent = findViewById(R.id.efab_transparent);
        efab_black = findViewById(R.id.efab_black);
        efab_white = findViewById(R.id.efab_white);

        iv.setOnClickListener(this);
        fab_switch.setOnClickListener(this);
        fab_save.setOnClickListener(this);
        efab_transparent.setOnClickListener(this);
        efab_black.setOnClickListener(this);
        efab_white.setOnClickListener(this);

        options = new BitmapFactory.Options();
        options.inDensity = DisplayMetrics.DENSITY_400;
        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            t3 = BitmapFactory.decodeResource(getResources(), R.drawable.t3_transparent, options);
            result = t3;
            emitter.onSuccess(result);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(bitmap -> {
                    iv.setImageBitmap(bitmap);
                    coordinatorLayout.setBackgroundColor(currentBgColor);
                    efab_black.setIcon(getDrawable(R.drawable.icon_check));
                    YoYo.with(Techniques.SlideInRight)
                            .duration(800)
                            .playOn(iv);
                })
                .subscribe();
        Single.create((SingleOnSubscribe<String>) emitter -> {
            tnt = BitmapFactory.decodeResource(getResources(), R.drawable.tnt_transparent, options);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new PicassoImageLoader());
        imagePicker.setCrop(false);
        imagePicker.setShowCamera(false);
        imagePicker.setSelectLimit(1);
    }

    public void selectImg(View view) {
        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, IMAGE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                ImageItem item = images.get(0);
                Log.d(TAG, "name: " + item.name);
                String regex = "Screenshot_[(0-9){2,4}|-]*.*.png";
                Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(item.name);
                if (matcher.find()) {
                    if (item.name.contains("锁屏")) {
                        Toast.makeText(this, "不支持的截图(锁屏,通知中心,后台管理)", Toast.LENGTH_SHORT).show();
                    } else {
                        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
                            if (current.equals("t3")) {
                                t3_two = BitmapFactory.decodeFile(item.path, options);
                                if (item.name.contains("计算器")) {
                                    t3_two = Bitmap.createBitmap(t3_two, 0, 0, t3_two.getWidth(), t3_two.getHeight() - 10);
                                }
                                result = compoundBitmap_t3(t3, t3_two);
                            } else if (current.equals("tnt")) {
                                tnt_two = BitmapFactory.decodeFile(item.path, options);
                                if (tnt_two.getWidth() < tnt_two.getHeight()) {
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(-90);
                                    tnt_two = Bitmap.createBitmap(tnt_two, 0, 0, tnt_two.getWidth(), tnt_two.getHeight(),
                                            matrix, true);
                                }
                                result = compoundBitmap_tnt(tnt, tnt_two);
                            }
                            emitter.onSuccess(result);
                        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .doOnSuccess(bitmap -> {
                                    iv.setImageBitmap(bitmap);
                                    YoYo.with(Techniques.FadeIn)
                                            .duration(500)
                                            .playOn(iv);
                                }).subscribe();
                    }
                } else {
                    Toast.makeText(this, "人家只支持截图图片哦 _(:з」∠)_", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "你怎么不选图片呢~~", Toast.LENGTH_SHORT).show();
            }
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
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filename)));
                emitter.onSuccess("成功");
            } catch (IOException e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(ret -> {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    saving.setVisibility(View.INVISIBLE);
                    fab_save.setVisibility(View.VISIBLE);
                })
                .doOnError(throwable -> {
                    Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                    saving.setVisibility(View.INVISIBLE);
                    fab_save.setVisibility(View.VISIBLE);
                })
                .subscribe();
    }

    public void switchFrame() {
        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            if (current.equals("t3")) {
                current = "tnt";
                result = tnt_two == null ? tnt : compoundBitmap_tnt(tnt, tnt_two);
            } else {
                current = "t3";
                result = t3_two == null ? t3 : compoundBitmap_t3(t3, t3_two);
            }
            emitter.onSuccess(result);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(bitmap -> {
                    YoYo.with(Techniques.SlideOutLeft)
                            .duration(500)
                            .onEnd(animator -> {
                                iv.setImageBitmap(result);
                                YoYo.with(Techniques.SlideInRight)
                                        .duration(500)
                                        .playOn(iv);
                            })
                            .playOn(iv);
                })
                .doOnError(throwable -> Log.d(TAG, "switchFrame: 切换出错" + throwable.getMessage()))
                .subscribe();
    }

    public void switchBg() {
        if (currentBgColor == Color.TRANSPARENT) {
            efab_transparent.setIcon(getDrawable(R.drawable.icon_check));
            efab_white.setIcon(getDrawable(R.drawable.icon_unchecked));
            efab_black.setIcon(getDrawable(R.drawable.icon_unchecked));
        } else if (currentBgColor == BgBlack) {
            efab_transparent.setIcon(getDrawable(R.drawable.icon_unchecked));
            efab_white.setIcon(getDrawable(R.drawable.icon_unchecked));
            efab_black.setIcon(getDrawable(R.drawable.icon_check));
        } else if (currentBgColor == BgWhite) {
            efab_transparent.setIcon(getDrawable(R.drawable.icon_unchecked));
            efab_white.setIcon(getDrawable(R.drawable.icon_check));
            efab_black.setIcon(getDrawable(R.drawable.icon_unchecked));
        }
        Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            if (current.equals("t3")) {
                result = t3_two == null ? t3 : compoundBitmap_t3(t3, t3_two);
            } else if (current.equals("tnt")) {
                result = tnt_two == null ? tnt : compoundBitmap_tnt(tnt, tnt_two);
            }
            emitter.onSuccess(result);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(bitmap -> {
                    iv.setImageBitmap(bitmap);
                    coordinatorLayout.setBackgroundColor(currentBgColor);
                })
                .subscribe();
    }

    private static float computeContrastBetweenColors(int bg, int fg) {
        float bgR = Color.red(bg) / 255f;
        float bgG = Color.green(bg) / 255f;
        float bgB = Color.blue(bg) / 255f;
        bgR = (bgR < 0.03928f) ? bgR / 12.92f : (float) Math.pow((bgR + 0.055f) / 1.055f, 2.4f);
        bgG = (bgG < 0.03928f) ? bgG / 12.92f : (float) Math.pow((bgG + 0.055f) / 1.055f, 2.4f);
        bgB = (bgB < 0.03928f) ? bgB / 12.92f : (float) Math.pow((bgB + 0.055f) / 1.055f, 2.4f);
        float bgL = 0.2126f * bgR + 0.7152f * bgG + 0.0722f * bgB;

        float fgR = Color.red(fg) / 255f;
        float fgG = Color.green(fg) / 255f;
        float fgB = Color.blue(fg) / 255f;
        fgR = (fgR < 0.03928f) ? fgR / 12.92f : (float) Math.pow((fgR + 0.055f) / 1.055f, 2.4f);
        fgG = (fgG < 0.03928f) ? fgG / 12.92f : (float) Math.pow((fgG + 0.055f) / 1.055f, 2.4f);
        fgB = (fgB < 0.03928f) ? fgB / 12.92f : (float) Math.pow((fgB + 0.055f) / 1.055f, 2.4f);
        float fgL = 0.2126f * fgR + 0.7152f * fgG + 0.0722f * fgB;

        return Math.abs((fgL + 0.05f) / (bgL + 0.05f));
    }

    public Bitmap compoundBitmap_t3(Bitmap one, Bitmap two) {
        one = one.copy(Bitmap.Config.ARGB_8888, true);
        two = two.copy(Bitmap.Config.ARGB_8888, true);
        two = Bitmap.createBitmap(two, 0, 82, two.getWidth(), two.getHeight() - 82);
        two = Bitmap.createScaledBitmap(two, 1080, 2155, true);
        int color = two.getPixel(two.getWidth() / 2, 1);
        Bitmap bar;
        if (computeContrastBetweenColors(color, Color.WHITE) < 3f) {
            Log.d(TAG, "compoundBitmap_t3: 白");
            bar = BitmapFactory.decodeResource(getResources(), R.drawable.bar_black, options);
        } else {
            Log.d(TAG, "compoundBitmap_t3: 黑");
            bar = BitmapFactory.decodeResource(getResources(), R.drawable.bar_white, options);
        }
        bar = bar.copy(Bitmap.Config.ARGB_8888, true);
        Log.d(TAG, "t3:" + one.getWidth() + "x" + one.getHeight());
        Log.d(TAG, "two:" + two.getWidth() + "x" + two.getHeight());
        Bitmap newBitmap = Bitmap.createBitmap(1826, 3252, Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();

//        Bitmap status_bg_origin = Bitmap.createBitmap(two, 0, 0, two.getWidth(), 2);
//        Bitmap status_bg = Bitmap.createScaledBitmap(status_bg_origin, 1080, 124, true);
        Bitmap status_bg = Bitmap.createBitmap(1080, 124, Bitmap.Config.ARGB_8888);
        status_bg.eraseColor(color);
//        Bitmap botton_bg_origin = Bitmap.createBitmap(two,0,two.getHeight()-2,two.getWidth(),2);
//        Bitmap bottom_bg = Bitmap.createScaledBitmap(botton_bg_origin,1080,121,true);
        Bitmap bottom_bg = Bitmap.createBitmap(1080, 131, Bitmap.Config.ARGB_8888);
        bottom_bg.eraseColor(two.getPixel(two.getWidth() / 2, two.getHeight() - 1));

        canvas.drawColor(currentBgColor);
        canvas.drawBitmap(transformer.blur(status_bg), 372, 426, paint);
        canvas.drawBitmap(bottom_bg, 372, 2698, paint);
        canvas.drawBitmap(two, 372, 550, paint);
        canvas.drawBitmap(bar, 373, 426, paint);
        canvas.drawBitmap(one, 0, 0, paint);
        canvas.save();
        canvas.restore();
        return newBitmap;
    }

    public Bitmap compoundBitmap_tnt(Bitmap one, Bitmap two) {
        one = one.copy(Bitmap.Config.ARGB_8888, true);
        two = two.copy(Bitmap.Config.ARGB_8888, true);
        two = Bitmap.createScaledBitmap(two, 1920, 1080, true);
        Bitmap result = Bitmap.createBitmap(3030, 2018, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        canvas.drawColor(currentBgColor);
        canvas.drawBitmap(two, 555, 297, paint);
        canvas.drawBitmap(one, 0, 0, paint);
        canvas.save();
        canvas.restore();
        return result;
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
            case R.id.fab_switch:
                switchFrame();
                break;
            case R.id.efab_transparent:
                currentBgColor = Color.TRANSPARENT;
                switchBg();
                break;
            case R.id.efab_white:
                currentBgColor = BgWhite;
                Log.d(TAG, "onClick: efab_white");
                switchBg();
                break;
            case R.id.efab_black:
                currentBgColor = BgBlack;
                switchBg();
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
