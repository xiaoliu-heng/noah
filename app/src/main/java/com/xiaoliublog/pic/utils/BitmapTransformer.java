package com.xiaoliublog.pic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class BitmapTransformer {

    private RenderScript rs;

    public BitmapTransformer(Context context) {
        rs = RenderScript.create(context);
    }

    public Bitmap blur(Bitmap bitmap){
        Bitmap blurredBitmap  = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        Allocation input = Allocation.createFromBitmap(rs,blurredBitmap,
                Allocation.MipmapControl.MIPMAP_FULL,
                Allocation.USAGE_SHARED);
        Allocation output = Allocation.createTyped(rs,input.getType());

        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        intrinsicBlur.setInput(input);
        intrinsicBlur.setRadius(20);
        intrinsicBlur.forEach(output);
        output.copyTo(blurredBitmap);
        bitmap.recycle();
        return blurredBitmap;
    }
}
