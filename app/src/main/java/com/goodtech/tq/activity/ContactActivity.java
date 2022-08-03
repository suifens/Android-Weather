package com.goodtech.tq.activity;

import android.os.Bundle;

import com.goodtech.tq.R;
import com.umeng.analytics.MobclickAgent;

public class ContactActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        findViewById(R.id.button_back).setOnClickListener(v -> finish());
    }
}
