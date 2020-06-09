package com.xiaoliublog.pic.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.var;

@Builder
public class ImageCombiner {
    @Builder.Default private final Canvas canvas = new Canvas();
    @Builder.Default private Paint paint = new Paint();
    @Singular("add")
    private List<ImageWithPosition> images;

    private int width, height;
    private int bgColor;

    public Bitmap combine() {
        var result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true);
        canvas.setBitmap(result);
        canvas.drawColor(bgColor);

        this.images.forEach(i -> canvas.drawBitmap(i.image, i.left, i.top, paint));

        canvas.save();
        canvas.restore();
        return result;
    }
}
