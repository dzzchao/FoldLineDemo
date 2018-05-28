package com.dzzchao.foldlinedemo.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dzzchao.foldlinedemo.HeartRateLineView;
import com.dzzchao.foldlinedemo.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {


    public SecondFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        Map<Float, Integer> map = new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            map.put(Float.parseFloat(i + ""), random.nextInt(100) + 30);
//            map.put(Float.parseFloat(i + ""), 60);
        }

        final HeartRateLineView heartRateLineView = view.findViewById(R.id.heartrate_lineview_second);
        heartRateLineView.setMapHeartrateData(map);
        heartRateLineView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                heartRateLineView.getParent().requestDisallowInterceptTouchEvent(true);
                heartRateLineView.setmIsConsumeEvent(true);
                return true;
            }
        });
        return view;

    }

}
