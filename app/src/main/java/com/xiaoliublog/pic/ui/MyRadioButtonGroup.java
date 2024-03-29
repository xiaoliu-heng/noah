package com.xiaoliublog.pic.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class MyRadioButtonGroup extends RadioGroup {
    public MyRadioButtonGroup(Context context) {
        super(context);
    }

    public MyRadioButtonGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof MyRadioButton){
            final MyRadioButton button = (MyRadioButton) child;
            if (button.isChecked()){
                setCheckedStateForView(button.getId(), true);
            }else {
                setCheckedStateForView(button.getId(), false);
            }
        }
        super.addView(child, index, params);
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView instanceof MyRadioButton) {
            final MyRadioButton button = (MyRadioButton) checkedView;
            button.setChecked(checked);
        }
    }
}
