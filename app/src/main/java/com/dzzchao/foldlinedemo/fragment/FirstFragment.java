package com.dzzchao.foldlinedemo.fragment;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
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
public class FirstFragment extends Fragment {


    public FirstFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        Map<Float, Integer> map = new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            map.put(Float.parseFloat(i + ""), random.nextInt(100) + 30);
//            map.put(Float.parseFloat(i + ""), 60);
        }

        final HeartRateLineView heartRateLineView = view.findViewById(R.id.heartrate_lineview_first);
        heartRateLineView.setMapHeartrateData(map);
        heartRateLineView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //父容器不需要拦截
                heartRateLineView.getParent().requestDisallowInterceptTouchEvent(true);
                heartRateLineView.setmIsConsumeEvent(true);
                return true;
            }
        });

        return view;
    }

}
