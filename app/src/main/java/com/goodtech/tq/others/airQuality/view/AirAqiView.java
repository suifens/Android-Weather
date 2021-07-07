package com.goodtech.tq.others.airQuality.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.views.CircleProgressView;

public class AirAqiView extends ConstraintLayout {

    public AirAqiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AirAqiView(Context context) {
        super(context);
        init();
    }


    private CircleProgressView mProgressView;
    private TextView mAqiTv;
    private TextView mAqiTypeTv;
    private TextView mAqiRemindTv;

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.air_stub_value, this, true);
        mProgressView = findViewById(R.id.progress_view);
        mAqiTv = findViewById(R.id.tv_air_aqi);
        mAqiTypeTv = findViewById(R.id.tv_aqi_type);
        mAqiRemindTv = findViewById(R.id.tv_aqi_remind);
    }


}
