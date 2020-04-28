package com.xiaoliublog.pic;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.xiaoliublog.pic.utils.BitmapTransformer;

public class MainActivityViewModel extends AndroidViewModel {
    public static int FRAME_T3 = 1;
    public static int FRAME_TNT = 2;

    private Bitmap _t3, _tnt;
    private int currentFrame = 1;
    private static final String TAG = "hyl";
    private BitmapTransformer transformer = new BitmapTransformer(this.getApplication());

    MutableLiveData<Bitmap> result = new MutableLiveData<>();
    MutableLiveData<Bitmap> t3_two = new MutableLiveData<>();
    MutableLiveData<Bitmap> tnt_two = new MutableLiveData<>();
    MutableLiveData<Bitmap> two = new MutableLiveData<>();
    MutableLiveData<Integer> currentBgColor = new MutableLiveData<>(Color.parseColor("#242424"));

    private BitmapFactory.Options options = new BitmapFactory.Options();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        options.inDensity = DisplayMetrics.DENSITY_400;
        _t3 = BitmapFactory.decodeResource(this.getApplication().getResources(), R.drawable.t3_transparent, options);
        result.postValue(_t3);
        _tnt = BitmapFactory.decodeResource(this.getApplication().getResources(), R.drawable.tnt_transparent, options);
    }

    private void reRender() {
        Log.d(TAG, "reRender: ");
        if (currentFrame == 2) {
            if (two.getValue() != null) {
                Bitmap r = compoundBitmap_tnt(_tnt, two.getValue());
                result.postValue(r);
                return;
            }
            result.postValue(_tnt);
        } else {
            if (two.getValue() != null) {
                Bitmap r = compoundBitmap_t3(_t3, two.getValue());
                result.postValue(r);
                return;
            }
            result.postValue(_t3);
        }
    }

    public void setFrame(int frame) {
        currentFrame = frame;
        reRender();
    }

    public void setTwo(Bitmap bitmap) {
        two.postValue(bitmap);
        if (currentFrame == 2) {
            Bitmap r = compoundBitmap_tnt(_tnt, bitmap);
            result.postValue(r);
        } else {
            Bitmap r = compoundBitmap_t3(_t3, bitmap);
            result.postValue(r);
        }
    }


    public void setCurrentBgColor(Integer color) {
        Log.d(TAG, "setCurrentBgColor: " + color + "," + currentBgColor.getValue());
        if (!color.equals(currentBgColor.getValue())) {
            currentBgColor.setValue(color);
            reRender();
        }
    }

//    private Bitmap changeBg(int color){
//        Bitmap bitmap, result;
//        if (currentFrame==1){
//            bitmap = _t3.copy(Bitmap.Config.ARGB_8888, true);
//            result = Bitmap.createBitmap(1826, 3252, Bitmap.Config.ARGB_8888, true);
//        }else {
//            bitmap = _tnt.copy(Bitmap.Config.ARGB_8888, true);
//            result = Bitmap.createBitmap(3030, 2018, Bitmap.Config.ARGB_8888, true);
//        }
//        Canvas canvas = new Canvas(result);
//        Paint paint = new Paint();
//        canvas.drawColor(currentBgColor.getValue());
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//        canvas.save();
//        canvas.restore();
//        return result;
//    }

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
        if (two.getWidth() > two.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            two = Bitmap.createBitmap(two, 0, 0, two.getWidth(), two.getHeight(),
                    matrix, true);
        }
        two = Bitmap.createBitmap(two, 0, 82, two.getWidth(), two.getHeight() - 82);
        two = Bitmap.createScaledBitmap(two, 1080, 2155, true);
        int color = two.getPixel(two.getWidth() / 2, 1);
        Bitmap bar;
        if (computeContrastBetweenColors(color, Color.WHITE) < 3f) {
            Log.d(TAG, "compoundBitmap_t3: 白");
            bar = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.bar_black, options);
        } else {
            Log.d(TAG, "compoundBitmap_t3: 黑");
            bar = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.bar_white, options);
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

        canvas.drawColor(currentBgColor.getValue());
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
        if (two.getWidth() < two.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            two = Bitmap.createBitmap(two, 0, 0, two.getWidth(), two.getHeight(),
                    matrix, true);
        }
        two = Bitmap.createScaledBitmap(two, 1920, 1080, true);
        Bitmap result = Bitmap.createBitmap(3030, 2018, Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        canvas.drawColor(currentBgColor.getValue());
        canvas.drawBitmap(two, 555, 297, paint);
        canvas.drawBitmap(one, 0, 0, paint);
        canvas.save();
        canvas.restore();
        return result;
    }
}
