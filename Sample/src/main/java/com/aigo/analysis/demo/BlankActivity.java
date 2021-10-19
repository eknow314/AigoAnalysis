package com.aigo.analysis.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.aigo.analysis.AigoAnalysisHelper;
import com.aigo.analysis.extra.TrackHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlankActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(v -> {
            TrackHelper.track()
                    .custom("e1", "e2")
                    .setExtension("n1", "v1")
                    .setExtension("n2", "v2")
                    .with(AigoAnalysisHelper.getInstance().getTracker());


        });
    }


    public static Date string2Date(String dateString, String style) {
        if (TextUtils.isEmpty(dateString)) return null;
        Date date = new Date();
        SimpleDateFormat strToDate = new SimpleDateFormat(style, Locale.getDefault());
        try {
            date = strToDate.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}