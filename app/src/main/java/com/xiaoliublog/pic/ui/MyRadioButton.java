package com.xiaoliublog.pic.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;

public class MyRadioButton extends AppCompatRadioButton {
    public MyRadioButton(Context context) {
        super(context);
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void drawUnSelected(Canvas canvas){
        canvas.save();
        this.getPaint().setColor(Color.WHITE);
        canvas.drawRoundRect(5, 10, getWidth()-5, getHeight()-10, 10, 10, getPaint());
        canvas.restore();
    }

    private void drawSelected(Canvas canvas){
        canvas.save();
        this.getPaint().setColor(Color.BLACK);
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 10, 10, getPaint());
        canvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isChecked()){
            drawSelected(canvas);
        }else {
            drawUnSelected(canvas);
        }
    }

}
