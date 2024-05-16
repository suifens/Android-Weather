package com.goodtech.tq.activity;

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

import com.goodtech.tq.R;
import com.goodtech.tq.modules.citySearch.CitySearchActivity;
import com.goodtech.tq.eventbus.CityEvent;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.modules.others.test.PrivacyWebActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.DisagreeAlert;
import com.goodtech.tq.views.DisagreeAlert.DisagreeAlertListener;
import com.goodtech.tq.utils.SpUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

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
                PrivacyWebActivity.redirectTo(PermissionActivity.this,
                        Constants.URL_AGREEMENT,
                        getResources().getString(R.string.title_agreement),
                        "Agreement");
            }
        };
        spannableString.setSpan(clickableSpan, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        ClickableSpan privateClickable = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                PrivacyWebActivity.redirectTo(PermissionActivity.this,
                        Constants.URL_PRIVACY,
                        getResources().getString(R.string.title_private),
                        "Privacy");
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
                onStartWeather(false);
                break;
            case R.id.button_disagree: {
                DisagreeAlert alert = new DisagreeAlert(PermissionActivity.this,
                        new DisagreeAlertListener() {
                            @Override
                            public void onConfirmClick(View view) {
                                onStartWeather(false);
                            }

                            @Override
                            public void onCancelClick(View view) {
                                finish();
                            }

                            @Override
                            public void onAgreementClick(View view) {
                                PrivacyWebActivity.redirectTo(PermissionActivity.this,
                                        Constants.URL_AGREEMENT,
                                        getResources().getString(R.string.title_agreement),
                                        "Agreement");
                            }

                            @Override
                            public void onPrivateClick(View view) {
                                PrivacyWebActivity.redirectTo(PermissionActivity.this,
                                        Constants.URL_PRIVACY,
                                        getResources().getString(R.string.title_private),
                                        "Privacy");
                            }

                            @Override
                            public void onVisitorClick(View view) {
                                onStartWeather(true);
                            }
                        });
                if (!isFinishing()) {
                    alert.show();
                }
            }
            break;
        }
    }

    private void onStartWeather(boolean isVisitor) {
        mHandler.post(() -> {
            SpUtils.getInstance().setPermissionAgree(!isVisitor);
            SpUtils.getInstance().putString(SpUtils.VERSION_APP, DeviceUtils.getVersionName(this));
            if (isVisitor) {
                showVisitor();
            } else {
                CitySearchActivity.redirectTo(this, true);
                this.finish();
            }
        });
    }

    //  游客
    private void showVisitor() {
        TipHelper.showProgressDialog(this);
        CityMode cityMode = new CityMode();
        cityMode.setCid(110100);
        cityMode.setMergerName("北京市");
        cityMode.setCity("北京市");
        cityMode.setLat("39.904989");
        cityMode.setLon("116.405285");
        cityMode.setPinyin("Beijing");
        LocationSpHelper.addCity(cityMode);

        WeatherHttpHelper helper = new WeatherHttpHelper(getApplicationContext());
        helper.getBaseUrl(() -> helper.fetchWeather(cityMode));
        EventBus.getDefault().post(new CityEvent().addCity(true));

        Intent intent = new Intent(PermissionActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        mHandler.postDelayed(this::finish, 300);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
