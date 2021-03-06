package com.goodtech.tq;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.DisagreeAlert;
import com.goodtech.tq.utils.DisagreeAlert.DisagreeAlertListener;
import com.goodtech.tq.utils.SpUtils;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("NonConstantResourceId")
public class PermissionActivity extends BaseActivity implements View.OnClickListener {

    private static final String boldStr = "请仔细阅读《隐私政策》及《用户协议》内容,我们将严格按照前述政策，为您提供更好的服务。若您是14岁以下未成年人，请您务必要求您的监护人仔细阅读本协议，并在征得您的监护人同意的前提下使用我们的产品。";
    private static final String agreementStr = "《用户协议》";
    private static final String privateStr = "《隐私政策》";
    private TextView mSpannableTv;

    public static void redirectTo(Context ctx) {
        Intent intent = new Intent(ctx, PermissionActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        View stationBar = findViewById(R.id.status);
        LinearLayout.LayoutParams bars = new LinearLayout.LayoutParams(stationBar.getLayoutParams());
        bars.height = bars.height + DeviceUtils.getStatusBarHeight();
        stationBar.setLayoutParams(bars);

        mSpannableTv = findViewById(R.id.tv_spannable);

        configSpannable();

        findViewById(R.id.button_agree).setOnClickListener(this);
        findViewById(R.id.button_disagree).setOnClickListener(this);
    }

    private void configSpannable() {
        String permissionStr = getString(R.string.permission_title1);
        SpannableString spannableString = new SpannableString(permissionStr);
        int agreementStart = permissionStr.indexOf(agreementStr);
        int agreementEnd = agreementStart + agreementStr.length();
        int privateStart = permissionStr.indexOf(privateStr);
        int privateEnd = privateStart + privateStr.length();
        int boldStart = permissionStr.indexOf(boldStr);
        int boldEnd = boldStart + boldStr.length();
        ForegroundColorSpan agreementColorSp = new ForegroundColorSpan(Color.parseColor("#00C4FF"));
        spannableString.setSpan(agreementColorSp, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ForegroundColorSpan privateColorSp = new ForegroundColorSpan(Color.parseColor("#00C4FF"));
        spannableString.setSpan(privateColorSp, privateStart, privateEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), boldStart, boldEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE); //粗体

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(PermissionActivity.this, AgreementActivity.class);
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        ClickableSpan privateClickable = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(PermissionActivity.this, PrivateActivity.class);
                startActivity(intent);
            }
        };
        spannableString.setSpan(privateClickable, privateStart, privateEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mSpannableTv.setMovementMethod(LinkMovementMethod.getInstance());
        mSpannableTv.setText(spannableString);
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
            case R.id.button_agree:
                checkAndRequestPermission();
                break;
            case R.id.button_disagree: {
                DisagreeAlert alert = new DisagreeAlert(PermissionActivity.this, new DisagreeAlertListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        checkAndRequestPermission();
                    }
                });
                if (!isFinishing()) {
                    alert.show();
                }
            }
            break;
        }
    }

    /**
     *
     * ----------非常重要----------
     *
     * Android6.0以上的权限适配简单示例：
     *
     * 如果targetSDKVersion >= 23，那么建议动态申请相关权限，再调用广点通SDK
     *
     * SDK不强制校验下列权限（即:无下面权限sdk也可正常工作），但建议开发者申请下面权限，尤其是READ_PHONE_STATE权限
     *
     * READ_PHONE_STATE权限用于允许SDK获取用户标识,
     * 针对单媒体的用户，允许获取权限的，投放定向广告；不允许获取权限的用户，投放通投广告，媒体可以选择是否把用户标识数据提供给优量汇，并承担相应广告填充和eCPM单价下降损失的结果。
     *
     * Demo代码里是一个基本的权限申请示例，请开发者根据自己的场景合理地编写这部分代码来实现权限申请。
     * 注意：下面的`checkSelfPermission`和`requestPermissions`方法都是在Android6.0的SDK中增加的API，如果您的App还没有适配到Android6.0以上，则不需要调用这些方法，直接调用广点通SDK即可。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        ArrayList<String> lackedPermissions = new ArrayList<>();

        if (checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            lackedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            lackedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            lackedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            lackedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            lackedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // 如果需要的权限都已经有了，那么直接调用SDK
        if (lackedPermissions.size() == 0) {
            onStartWeather();
        } else {
            String[] requestPermissions = new String[lackedPermissions.size()];
            lackedPermissions.toArray(requestPermissions);
            ActivityCompat.requestPermissions(this, requestPermissions, 1024);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1024) {
            onStartWeather();
        }

//        if (requestCode == 1024 && hasAllPermissionsGranted(grantResults)) {
//            onStartWeather();
//        } else {
//            Toast.makeText(this, "应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
//            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            intent.setData(Uri.parse("package:" + getPackageName()));
//            startActivity(intent);
//            finish();
//        }
    }

    private void onStartWeather() {
        SpUtils.getInstance().putString(SpUtils.VERSION_APP, "0");
        this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
