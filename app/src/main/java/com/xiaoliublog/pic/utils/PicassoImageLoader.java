package com.xiaoliublog.pic.utils;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.lzy.imagepicker.loader.ImageLoader;
import com.squareup.picasso.Picasso;
import com.xiaoliublog.pic.R;

import java.io.File;

public class PicassoImageLoader implements ImageLoader {
    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        Picasso.get()
                .load(Uri.fromFile(new File(path)))
                .placeholder(R.mipmap.icon_noah)
                .error(R.mipmap.icon_noah)
                .resize(width,height)
                .centerInside()
                .into(imageView);
    }

    @Override
    public void displayImagePreview(Activity activity, String path, ImageView imageView, int width, int height) {
        Picasso.get()
                .load(Uri.fromFile(new File(path)))
                .resize(width,height)
                .centerInside()
                .into(imageView);
    }

    @Override
    public void clearMemoryCache() {
        Picasso.get().shutdown();
    }
}
