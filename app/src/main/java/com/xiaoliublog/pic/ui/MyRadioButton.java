package com.xiaoliublog.pic.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;

import com.xiaoliublog.pic.R;

public class MyRadioButton extends AppCompatRadioButton {
    private static final String TAG = "hyl-radio";
    private Drawable selectedColor;
    private int shapeFlag = 0;
    private int mPadding = 1;

    public MyRadioButton(Context context) {
        super(context);
        init();
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyRadioButton);
        selectedColor = typedArray.getDrawable(R.styleable.MyRadioButton_selectedColor);
        shapeFlag = typedArray.getColor(R.styleable.MyRadioButton_shape, 0);
        typedArray.recycle();
        init();
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyRadioButton);
        selectedColor = typedArray.getDrawable(R.styleable.MyRadioButton_selectedColor);
        shapeFlag = typedArray.getColor(R.styleable.MyRadioButton_shape, 0);
        typedArray.recycle();
        init();
    }


    private void init() {
    }

    @Override
    public void setChecked(boolean checked) {
        ValueAnimator animator = ValueAnimator
                .ofInt(checked ? 5 : 1, checked ? 1 : 5)
                .setDuration(200);
        animator.addUpdateListener(animation -> {
            mPadding = (int) animation.getAnimatedValue();
            postInvalidate();
        });
        animator.start();
        super.setChecked(checked);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: " + getLeft() + "," + getTop() + "," + getRight() + "," + getBottom() + ". Bounds" + selectedColor.getBounds());
        if (shapeFlag == 0) {
            selectedColor.setBounds(mPadding, mPadding, getWidth() - mPadding, getHeight() - mPadding);
            selectedColor.draw(canvas);
        } else {
            selectedColor.setBounds(0, 0, getWidth(), getHeight());
            selectedColor.draw(canvas);
        }

    }

}
