package com.goodtech.tq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.goodtech.tq.util.Constant;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity1 extends AppCompatActivity {

    private static final String TAG = "SplashActivity1";

    private ConstraintLayout container;

    /**
     * 用于判断是否可以跳过广告，进入MainActivity
     */
    private boolean canJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);
        container = findViewById(R.id.container);
        //  进行运行时权限处理
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_MEDIA_LOCATION);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            requestAds();
        }
    }

    /**
     * 请求开屏广告
     */
    private void requestAds() {
        new SplashAD(this, container, Constant.APP_ID, Constant.AD_ID, new SplashADListener() {
            @Override
            public void onADDismissed() {
                //  广告显示完毕
//                forward();
            }

            @Override
            public void onNoAD(AdError var1) {
                //  广告加载失败
                forward();
            }

            @Override
            public void onADPresent() {
                //  广告加载成功
            }

            @Override
            public void onADClicked() {
                //  广告被点击
            }

            @Override
            public void onADTick(long var1) {

            }

            @Override
            public void onADExposure() {

            }

            @Override
            public void onADLoaded(long l) {

            }
        }, 200);
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJump = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            forward();
        }
    }

    private void forward() {
        if (canJump) {
            //  跳转到MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            canJump = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestAds();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
}
