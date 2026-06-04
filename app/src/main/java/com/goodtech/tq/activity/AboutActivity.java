package com.goodtech.tq.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.goodtech.tq.R;
import com.goodtech.tq.modules.others.test.PrivacyWebActivity;
import com.goodtech.tq.utils.Constants;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        configStationBar(findViewById(R.id.private_station_bar));

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.tv_private).setOnClickListener(this);
        findViewById(R.id.tv_agreement).setOnClickListener(this);

        TextView versionTv = findViewById(R.id.tv_version);
        versionTv.setText("版本：V " + AppUtils.getAppVersionName());
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
