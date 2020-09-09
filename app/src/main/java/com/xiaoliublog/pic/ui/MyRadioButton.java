package com.xiaoliublog.pic.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;

import com.xiaoliublog.pic.BackgroundColor;
import com.xiaoliublog.pic.R;

public class MyRadioButton extends AppCompatRadioButton {
    private Paint mPaint = new Paint();
    private Paint borderPaint = new Paint();
    private int selectedColor = Color.WHITE;
    private int shapeFlag = 0;
    private float mPadding = 1;

    public MyRadioButton(Context context) {
        super(context);
        init();
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyRadioButton);
        selectedColor = typedArray.getColor(R.styleable.MyRadioButton_selectedColor, Color.WHITE);
        shapeFlag = typedArray.getColor(R.styleable.MyRadioButton_shape, 0);
        typedArray.recycle();
        init();
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyRadioButton);
        selectedColor = typedArray.getColor(R.styleable.MyRadioButton_selectedColor, Color.WHITE);
        shapeFlag = typedArray.getColor(R.styleable.MyRadioButton_shape, 0);
        typedArray.recycle();
        init();
    }

    private void init() {
        Shader shader = new LinearGradient(getWidth() / 2f, 0, getWidth() / 2f, getHeight(), selectedColor, BackgroundColor.White, Shader.TileMode.CLAMP);
        mPaint.setShader(shader);
        mPaint.setColor(selectedColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setAntiAlias(true);
    }

    @Override
    public void setChecked(boolean checked) {
        ValueAnimator animator = ValueAnimator
                .ofFloat(checked ? 5 : 1, checked ? 1 : 5)
                .setDuration(200);
        animator.addUpdateListener(animation -> {
            mPadding = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        animator.start();
        super.setChecked(checked);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setStrokeWidth(2f);
        if (shapeFlag == 0) {
            canvas.drawRoundRect(mPadding, mPadding * 2, getWidth() - mPadding, getHeight() - mPadding * 2, 15 - mPadding, 15 - mPadding, mPaint);
            canvas.drawRoundRect(mPadding, mPadding * 2, getWidth() - mPadding, getHeight() - mPadding * 2, 15 - mPadding, 15 - mPadding, borderPaint);
        } else {
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (getWidth() - mPadding) / 2f - 2, mPaint);
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (getWidth() - mPadding) / 2f - 2, borderPaint);
        }
    }

}
