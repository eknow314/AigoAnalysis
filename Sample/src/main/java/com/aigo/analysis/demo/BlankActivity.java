package com.aigo.analysis.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;

public class BlankActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        findViewById(R.id.button).setOnClickListener(v -> {
            startActivity(new Intent(BlankActivity.this, BlankActivity2.class));
        });
    }
}