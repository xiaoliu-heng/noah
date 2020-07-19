package com.xiaoliublog.pic.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.xiaoliublog.pic.R;

/**
 * TODO: document your custom view class.
 */
public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Drawable mExampleDrawable;
    private Bitmap bitmap;

    private TextPaint mTextPaint;
    private Paint paint;

    int meshWidth = 3;
    int meshHeight = 3;
    int pointLength = (meshWidth+1)*(meshHeight+1);
    float[] verts = new float[pointLength*2];

    public MyImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        paint = new Paint();

    }


    @Override

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* TODO: consider storing these as member variables to reduce
         *  allocations per draw cycle.
         */
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        int centerX = paddingLeft + contentWidth/2;
        int centerY = paddingTop + contentHeight/2;

        if (bitmap != null){
            canvas.save();
            canvas.drawBitmap(bitmap,null,paint);
            canvas.restore();
        }

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
    }
    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
