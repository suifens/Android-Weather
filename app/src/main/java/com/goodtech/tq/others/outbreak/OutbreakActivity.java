package com.goodtech.tq.others.outbreak;

import android.os.Bundle;

import com.goodtech.tq.base.BaseWebActivity;

public class OutbreakActivity extends BaseWebActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitleView.setText("疫情出行");
        mUrl = "https://huijia.juhekeji.com/";
        mChannel = "Ac_Outbreak";
    }
}