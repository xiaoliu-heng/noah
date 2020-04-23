package com.xiaoliublog.pic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.yuyashuai.frameanimation.FrameAnimation;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "hyl-SplashActivity";
    SurfaceView textureView;
    FrameAnimation frameAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        textureView = findViewById(R.id.splash_img);
        frameAnimation = new FrameAnimation(textureView);
        frameAnimation.setRepeatMode(FrameAnimation.RepeatMode.ONCE);
        frameAnimation.setScaleType(FrameAnimation.ScaleType.FIT_CENTER);

        Handler handler = new Handler();
        Runnable runnable = this::goToMain;
        handler.postDelayed(runnable, 6006);
        textureView.setOnClickListener(v -> {
            handler.removeCallbacks(runnable);
            goToMain();
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        frameAnimation.playAnimationFromAssets("splash");
    }

    @Override
    protected void onPause() {
        super.onPause();
        frameAnimation.stopAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        frameAnimation.stopAnimation();
    }
}
