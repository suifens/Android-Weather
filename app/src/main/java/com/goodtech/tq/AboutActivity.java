package com.goodtech.tq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.utils.DeviceUtils;
import com.umeng.analytics.MobclickAgent;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

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
        setContentView(R.layout.activity_about);
        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        TextView versionTv = findViewById(R.id.tv_version);
        String version = DeviceUtils.getVersionName(AboutActivity.this);
        versionTv.setText(String.format("版本: V %s", version));

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.tv_agreement).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;

            case R.id.tv_agreement:
                Intent intent = new Intent(this, AgreementActivity.class);
                startActivity(intent);
                break;
        }
    }
}
