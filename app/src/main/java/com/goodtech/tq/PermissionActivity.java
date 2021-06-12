package com.goodtech.tq;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.DisagreeAlert;
import com.goodtech.tq.utils.DisagreeAlert.DisagreeAlertListener;
import com.goodtech.tq.utils.SpUtils;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("NonConstantResourceId")
public class PermissionActivity extends BaseActivity implements View.OnClickListener {

    private static final String boldStr = "请仔细阅读《隐私政策》及《用户协议》内容,我们将严格按照前述政策，为您提供更好的服务。若您是14岁以下未成年人，请您务必要求您的监护人仔细阅读本协议，并在征得您的监护人同意的前提下使用我们的产品。";
    private static final String agreementStr = "《用户协议》";
    private static final String privateStr = "《隐私政策》";
    private TextView mSpannableTv;
    private int mCancelTimes;

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
//                checkAndRequestPermission();
                onStartWeather();
                break;
            case R.id.button_disagree: {
                if (mCancelTimes > 0) {
                    finish();
                    return;
                }
                DisagreeAlert alert = new DisagreeAlert(PermissionActivity.this,
                        new DisagreeAlertListener() {
                            @Override
                            public void onConfirmClick(View view) {
                                mCancelTimes += 1;
                            }

                            @Override
                            public void onCancelClick(View view) {
                                finish();
                            }
                        });
                if (!isFinishing()) {
                    alert.show();
                }
            }
            break;
        }
    }

    private static final String TAG = "PermissionActivity";
    private void onStartWeather() {
        mHandler.post(() -> {
            SpUtils.getInstance().putString(SpUtils.VERSION_APP, DeviceUtils.getVersionName(this));
            CitySearchActivity.redirectTo(this, true);
            this.finish();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
