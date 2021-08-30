package com.aigo.analysis.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.aigo.analysis.demo.file.FileTestActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = findViewById(R.id.btn_1);
        btn1.setOnClickListener(v -> {
//            DeviceID.getOAID(MainActivity.this, new IGetter() {
//                @Override
//                public void onOAIDGetComplete(@NonNull String result) {
//                    btn1.setText("OAID：\n" + result);
//                }
//
//                @Override
//                public void onOAIDGetError(@NonNull Throwable error) {
//                    String DeviceId = DeviceID.getWidevineID();
//                    if (!TextUtils.isEmpty(DeviceId)) {
//                        btn1.setText("WidevineID：\n" + DeviceId);
//                        return;
//                    }
//                    DeviceId = DeviceID.getAndroidID(MainActivity.this);
//                    if (!TextUtils.isEmpty(DeviceId)) {
//                        btn1.setText("AndroidID：\n" + DeviceId);
//                        return;
//                    }
//                    DeviceId = DeviceID.getGUID(MainActivity.this);
//                    btn1.setText("GUID：\n" + DeviceId);
//                }
//            });
//
//            TrackerHelper.getInstance().with(new GetInitDataEvent());
//            TrackerHelper.getInstance().with(new UserLoginEvent("photo", "nickName", "account", "13800000000"));
        });

        findViewById(R.id.btn_2).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FileTestActivity.class));
        });


        findViewById(R.id.btn_3).setOnClickListener(v -> {

        });

        findViewById(R.id.btn_4).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BlankActivity.class));
        });

        findViewById(R.id.btn_5).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NavTestActivity.class));
        });

        findViewById(R.id.btn_6).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BlankActivity2.class));
        });

    }
}