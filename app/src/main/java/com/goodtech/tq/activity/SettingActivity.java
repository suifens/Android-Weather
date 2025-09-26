package com.goodtech.tq.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.bytedance.sdk.djx.DJXSdk;
import com.bytedance.sdk.dp.DPSdk;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.goodtech.tq.BuildConfig;
import com.goodtech.tq.R;
import com.goodtech.tq.ad.AdFeedActivity;
import com.goodtech.tq.modules.others.test.MyTestActivity;
import com.goodtech.tq.modules.others.test.PrivacyWebActivity;
import com.goodtech.tq.modules.removeAd.RemoveAdActivity;
import com.goodtech.tq.modules.video.DrawVideoFullScreenActivity;
import com.goodtech.tq.modules.video.djx.DrawDramaActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;

public class SettingActivity extends AdFeedActivity implements View.OnClickListener {

    private static final String grantedStr = "已允许";
    private static final String deniedStr = "权限设置";
    private static final int grantedColor = Color.parseColor("#9B9B9B");
    private static final int deniedColor = Color.parseColor("#00C4FF");

    private ToggleButton mSwitchView;
    private ToggleButton mToggleSwitch;
    private View adContainer;
    private TTFeedAd mFeedAd;
    private boolean mLoadSuccess = false;
    
    // 悬浮按钮
    private ImageButton mFabRemoveAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mSwitchView = findViewById(R.id.switchBtn_setting_m);
        updateAdType(SpUtils.getInstance().getBoolean(Constants.PERSONALIZED_AD, true));
        //  异常天气提醒
        mToggleSwitch = findViewById(R.id.switchBtn_reminder);
        boolean checked = SpUtils.getInstance().getBoolean(Constants.REMINDER_WEATHER, true);
        mToggleSwitch.setChecked(checked);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        adContainer = findViewById(R.id.ad_contentPanel);

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.layout_widget).setOnClickListener(this);
        findViewById(R.id.layout_private_list).setOnClickListener(this);
//        findViewById(R.id.layout_praise).setOnClickListener(this);
        findViewById(R.id.layout_about).setOnClickListener(this);
        findViewById(R.id.layout_contact).setOnClickListener(this);
        findViewById(R.id.layout_version).setOnClickListener(this);
        findViewById(R.id.layout_permission_phone).setOnClickListener(this);
        findViewById(R.id.layout_permission_storage).setOnClickListener(this);
        findViewById(R.id.layout_permission_location).setOnClickListener(this);
        findViewById(R.id.switchBtn_setting_m).setOnClickListener(this);
        findViewById(R.id.switchBtn_reminder).setOnClickListener(this);
        findViewById(R.id.short_play_btn).setOnClickListener(this);
        findViewById(R.id.mini_video_btn).setOnClickListener(this);
        findViewById(R.id.layout_privacy).setOnClickListener(this);
        findViewById(R.id.layout_agreement).setOnClickListener(this);
        findViewById(R.id.layout_info_list).setOnClickListener(this);
        findViewById(R.id.layout_share_list).setOnClickListener(this);
        
        // 设置悬浮按钮
        setupFab();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected Boolean isFirstLoad = true;
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        checkPermissions();
        if (isFirstLoad) {
            isFirstLoad = false;
            if (SpUtils.getInstance().isAgreePermission()) {
                loadFeedAd();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @SuppressLint({"QueryPermissionsNeeded", "NonConstantResourceId"})
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            finish();
        } else if (v.getId() == R.id.layout_widget) {
            Intent intent = new Intent(SettingActivity.this, WidgetSettingActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.layout_private_list) {
            MyTestActivity.redirectTo(this,
                    "https://app.yiguxm.com/privacy/yuzhiyinsiqingdan.html",
                    getResources().getString(R.string.title_private_list),
                    "Privacy_List");
        } else if (v.getId() == R.id.layout_version) { // (v.getId() == R.id.layout_praise || v.getId() == R.id.layout_version) {
            // 评论
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // 要调起的应用不存在时的处理
                Toast.makeText(this, "未能跳转到应用商店", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.layout_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.layout_contact) {
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.layout_permission_phone || v.getId() == R.id.layout_permission_storage) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(intent);
        } else if (v.getId() == R.id.layout_permission_location) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else if (v.getId() == R.id.switchBtn_setting_m) {
            // 个性化设置
            boolean show = SpUtils.getInstance().getBoolean(Constants.PERSONALIZED_AD, true);
            SpUtils.getInstance().putBoolean(Constants.PERSONALIZED_AD, !show);
            updateAdType(!show);
            // GlobalSetting.setAgreePrivacyStrategy(!show);
        } else if (v.getId() == R.id.switchBtn_reminder) {
            updateReminder();
        } else if (v.getId() == R.id.short_play_btn) {
            if (DJXSdk.isStartSuccess()) {
                DrawDramaActivity.start(this);
            }
        } else if (v.getId() == R.id.mini_video_btn) {
            if (DPSdk.isStartSuccess()) {
                Intent intent = new Intent(this, DrawVideoFullScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.layout_agreement) {
            PrivacyWebActivity.redirectTo(this,
                    Constants.URL_AGREEMENT,
                    getResources().getString(R.string.title_agreement),
                    "Agreement");
        } else if (v.getId() == R.id.layout_privacy) {
            PrivacyWebActivity.redirectTo(this,
                    Constants.URL_PRIVACY,
                    getResources().getString(R.string.title_private),
                    "Privacy");
        } else if (v.getId() == R.id.layout_info_list) {
            PrivacyWebActivity.redirectTo(this,
                    Constants.URL_INFO_LIST,
                    getResources().getString(R.string.title_info_list),
                    "Info List");
        } else if (v.getId() == R.id.layout_share_list) {
            PrivacyWebActivity.redirectTo(this,
                    Constants.URL_SHARE_LIST,
                    getResources().getString(R.string.title_share_list),
                    "Share List");
        }
    }

    /**
     * 设置悬浮按钮
     * 初始化去广告悬浮按钮并设置点击事件
     */
    private void setupFab() {
        mFabRemoveAd = findViewById(R.id.fab_remove_ad);
        if (mFabRemoveAd != null) {
            mFabRemoveAd.setOnClickListener(v -> {
                // 启动去广告页面
                RemoveAdActivity.startActivity(SettingActivity.this);
            });
        }
    }

    private void updateAdType(boolean show) {
        if (mSwitchView != null) {
            mSwitchView.setChecked(show);
        }
    }

    private void updateReminder() {
        boolean checked = mToggleSwitch.isChecked();
        if (checked) {
            JPushInterface.resumePush(this);
        } else {
            JPushInterface.stopPush(this);
        }
        SpUtils.getInstance().putBoolean(Constants.REMINDER_WEATHER, checked);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
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

    private void loadFeedAd() {
        if (mLoadSuccess) return;
        int adWidth = SizeUtils.px2dp(ScreenUtils.getScreenWidth()) - 20;
        loadFeedAd(BuildConfig.PGE_CALENDAR_POS_ID, adWidth, (data, errorMsg) -> {
            if (data != null) {
                mHandler.post(() -> {
                    mFeedAd = data;
                    mLoadSuccess = true;
                    showAd((FrameLayout) adContainer, mLoadSuccess, true, mFeedAd);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFeedAd != null) {
            try {
                mFeedAd.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
