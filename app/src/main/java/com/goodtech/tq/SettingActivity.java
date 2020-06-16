package com.goodtech.tq;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.layout_praise).setOnClickListener(this);
        findViewById(R.id.layout_about).setOnClickListener(this);
    }

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.layout_praise: {
                //  评论
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
                break;
            case R.id.layout_about: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
                break;
        }
    }
}
