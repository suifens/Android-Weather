package com.goodtech.tq;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.goodtech.tq.utils.TipHelper;
import com.umeng.analytics.MobclickAgent;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private static final String grantedStr = "已允许";
    private static final String deniedStr = "权限设置";
    private static final int grantedColor = Color.parseColor("#9B9B9B");
    private static final int deniedColor = Color.parseColor("#00C4FF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.layout_praise).setOnClickListener(this);
        findViewById(R.id.layout_about).setOnClickListener(this);
        findViewById(R.id.layout_version).setOnClickListener(this);
        findViewById(R.id.layout_permission_phone).setOnClickListener(this);
        findViewById(R.id.layout_permission_storage).setOnClickListener(this);
        findViewById(R.id.layout_permission_location).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        checkPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.layout_praise:
            case R.id.layout_version: {
                //  评论
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    //要调起的应用不存在时的处理
                    Toast.makeText(this, "未能跳转到应用商店", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case R.id.layout_about: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.layout_permission_phone:
            case R.id.layout_permission_storage: {

                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
            break;
            case R.id.layout_permission_location: {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        TextView phoneStateTv = findViewById(R.id.tv_state_phone);
        if (checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            phoneStateTv.setText(deniedStr);
            phoneStateTv.setTextColor(deniedColor);
        } else {
            phoneStateTv.setText(grantedStr);
            phoneStateTv.setTextColor(grantedColor);
        }

        TextView locationStateTv = findViewById(R.id.tv_state_location);
        if (isLocationServicesAvailable(this)) {
            locationStateTv.setText(grantedStr);
            locationStateTv.setTextColor(grantedColor);
        } else {
            locationStateTv.setText(deniedStr);
            locationStateTv.setTextColor(deniedColor);
        }

        TextView storageStateTv = findViewById(R.id.tv_state_storage);
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                || checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            storageStateTv.setText(deniedStr);
            storageStateTv.setTextColor(deniedColor);
        } else {
            storageStateTv.setText(grantedStr);
            storageStateTv.setTextColor(grantedColor);
        }

    }

    private boolean isLocationServicesAvailable(Context context) {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }
        boolean coarsePermissionCheck = !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean finePermissionCheck = !checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        return isAvailable && (coarsePermissionCheck || finePermissionCheck);
    }
}
