package com.dzzchao.foldlinedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private HeartRateLineView heartRateLineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Map<Float, Integer> map = new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            map.put(Float.parseFloat(i + ""), random.nextInt(100) + 30);
//            map.put(Float.parseFloat(i + ""), 60);
        }

        heartRateLineView = findViewById(R.id.heartrate_lineview);
        heartRateLineView.setMapHeartrateData(map);
        heartRateLineView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                heartRateLineView.getParent().requestDisallowInterceptTouchEvent(true);
                heartRateLineView.setmIsConsumeEvent(true);
                return true;
            }
        });

    }
}