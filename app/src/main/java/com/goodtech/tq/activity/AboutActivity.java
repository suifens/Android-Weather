package com.goodtech.tq.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.modules.others.test.PrivacyWebActivity;
import com.goodtech.tq.utils.Constants;
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
        findViewById(R.id.tv_private).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            finish();
        } else if (v.getId() == R.id.tv_agreement) {
            PrivacyWebActivity.redirectTo(this,
                    Constants.URL_AGREEMENT,
                    getResources().getString(R.string.title_agreement),
                    "Agreement");
        } else if (v.getId() == R.id.tv_private) {
            PrivacyWebActivity.redirectTo(this,
                    Constants.URL_PRIVACY,
                    getResources().getString(R.string.title_private),
                    "Privacy");
        }
    }
}
