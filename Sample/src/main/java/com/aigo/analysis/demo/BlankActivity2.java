package com.aigo.analysis.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import timber.log.Timber;

public class BlankActivity2 extends AppCompatActivity {

    LottieAnimationView lottieView_device_init;
    private AppCompatActivity mActivity;

    float maxFrame = 899;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank2);
        mActivity = this;
        lottieView_device_init = findViewById(R.id.lottieView_device_init);


        lottieView_device_init.setMinAndMaxFrame(0, 179);
        lottieView_device_init.setRepeatCount(ValueAnimator.INFINITE);
        lottieView_device_init.playAnimation();


        findViewById(R.id.btn_1).setOnClickListener(v -> {
            Toast.makeText(mActivity, "第一步完成", Toast.LENGTH_LONG).show();
            lottieView_device_init.setMinAndMaxFrame(0, 314);
            lottieView_device_init.addAnimatorUpdateListener(updateListener1);
        });

        findViewById(R.id.btn_2).setOnClickListener(v -> {
            Toast.makeText(mActivity, "第二步完成", Toast.LENGTH_LONG).show();
            lottieView_device_init.setMinAndMaxFrame(0, 629);
            lottieView_device_init.addAnimatorUpdateListener(updateListener2);
        });

        findViewById(R.id.btn_3).setOnClickListener(v -> {
            Toast.makeText(mActivity, "第三步完成", Toast.LENGTH_LONG).show();
            lottieView_device_init.setMinAndMaxFrame(0, 899);
            lottieView_device_init.setRepeatCount(0);
        });

        findViewById(R.id.btn_4).setOnClickListener(v -> {
            Toast.makeText(mActivity, "重试", Toast.LENGTH_LONG).show();
            lottieView_device_init.setMinAndMaxFrame(0, 179);
            lottieView_device_init.setRepeatCount(ValueAnimator.INFINITE);
            lottieView_device_init.playAnimation();
        });

    }

    private ValueAnimator.AnimatorUpdateListener updateListener1 = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float ts = (float) animation.getAnimatedValue();
            if (Math.round(ts * maxFrame) == 314) {
                Timber.e("updateListener1111111111");
                lottieView_device_init.setMinAndMaxFrame(270, 314);
                lottieView_device_init.removeUpdateListener(updateListener1);
            }
        }
    };

    private ValueAnimator.AnimatorUpdateListener updateListener2 = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float ts = (float) animation.getAnimatedValue();
            if (Math.round(ts * maxFrame) == 629) {
                Timber.e("updateListener222222222");
                lottieView_device_init.setMinAndMaxFrame(540, 629);
                lottieView_device_init.removeUpdateListener(updateListener2);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}