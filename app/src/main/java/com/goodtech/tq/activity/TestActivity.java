package com.goodtech.tq.activity;


import android.app.Activity;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.goodtech.tq.R;
import com.goodtech.tq.views.CircleProgressView;
import com.goodtech.tq.views.IArcView;

public class TestActivity extends Activity {

    IArcView mArcView;
    CircleProgressView mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mProgressView = findViewById(R.id.progress_view);
        if (mProgressView != null) {
            mProgressView.setAngle(80);
        }
    }
}