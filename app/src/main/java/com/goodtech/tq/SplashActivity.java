package com.goodtech.tq;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.news.NewsActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.SpUtils;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.umeng.commonsdk.UMConfigure;

/**
 * 这是demo工程的入口Activity，在这里会首次调用广点通的SDK。
 *
 * 在调用SDK之前，如果您的App的targetSDKVersion >= 23，那么建议动态申请相关权限。
 */
public class SplashActivity extends Activity implements SplashADListener, View.OnClickListener {

    private SplashAD splashAD;
    private ViewGroup container;
    private TextView skipView;
    private static final String SKIP_TEXT = "点击跳过 %d";

    public boolean canJump = false;

    /**
     * 记录拉取广告的时间
     */
    private long fetchSplashADTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        container = this.findViewById(R.id.splash_container);

//        ApiClient.getInstance();

//        getChannelName(this);
    }

    private String getPosId() {
        String posId = getIntent().getStringExtra("ad_id");
        return TextUtils.isEmpty(posId) ? Constants.SPLASH_POS_ID : posId;
    }

    /**
     * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
     * @param activity        展示广告的activity
     * @param adContainer     展示广告的大容器
     * @param skipContainer   自定义的跳过按钮：传入该view给SDK后，SDK会自动给它绑定点击跳过事件。SkipView的样式可以由开发者自由定制，其尺寸限制请参考activity_splash.xml或者接入文档中的说明。
     * @param posId           广告位ID
     * @param adListener      广告状态监听器
     */
    private void fetchSplashAD(Activity activity, ViewGroup adContainer, View skipContainer,
                               String posId, SplashADListener adListener) {
        fetchSplashADTime = System.currentTimeMillis();
        splashAD = new SplashAD(activity, skipContainer, Constants.APP_ID, posId, adListener, 0);
        splashAD.fetchAndShowIn(adContainer);
    }

    @Override
    public void onADPresent() {
        Log.i("AD_DEMO", "SplashADPresent");
    }

    @Override
    public void onADClicked() {
        Log.i("AD_DEMO", "SplashADClicked clickUrl: "
                + (splashAD.getExt() != null ? splashAD.getExt().get("clickUrl") : ""));
    }

    /**
     * 倒计时回调，返回广告还将被展示的剩余时间。
     * 通过这个接口，开发者可以自行决定是否显示倒计时提示，或者还剩几秒的时候显示倒计时
     *
     * @param millisUntilFinished 剩余毫秒数
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onADTick(long millisUntilFinished) {
        Log.i("AD_DEMO", "SplashADTick " + millisUntilFinished + "ms");
        if (skipView != null) {
            skipView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            skipView.setText(String.format(SKIP_TEXT, Math.round(millisUntilFinished / 1000f)));
        }
    }

    @Override
    public void onADExposure() {
        Log.i("AD_DEMO", "SplashADExposure");
    }

    @Override
    public void onADLoaded(long expireTimestamp) {
        Log.i("AD_DEMO", "SplashADFetch expireTimestamp:"+expireTimestamp);
    }

    @Override
    public void onADDismissed() {
        Log.i("AD_DEMO", "SplashADDismissed");
        next();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onNoAD(AdError error) {
//        final String str = String.format("LoadSplashADFail, eCode=%d, errorMsg=%s", error.getErrorCode(),
//                error.getErrorMsg());
//        Log.i("AD_DEMO",str);
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(SplashActivity.this.getApplicationContext(), str, Toast.LENGTH_SHORT).show();
//            }
//        });

        //为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
        //给出的延时逻辑是从拉取广告开始算开屏最少持续多久
        long alreadyDelayMills = System.currentTimeMillis() - fetchSplashADTime;//从拉广告开始到onNoAD已经消耗了多少时间
        int minSplashTimeWhenNoAD = 2000;
        long shouldDelayMills = alreadyDelayMills > minSplashTimeWhenNoAD ? 0 : minSplashTimeWhenNoAD
                - alreadyDelayMills;//为防止加载广告失败后立刻跳离开屏可能造成的视觉上类似于"闪退"的情况，根据设置的minSplashTimeWhenNoAD
        // 计算出还需要延时多久
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SplashActivity.this.onStartWeather();
            }
        }, shouldDelayMills);
    }

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     */
    private void next() {
        if (canJump) {
            this.onStartWeather();
        } else {
            canJump = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  配置 UM_APP_ID , 标识
        UMConfigure.init(this, Constants.UM_APP_ID, BuildConfig.FLAVOR, UMConfigure.DEVICE_TYPE_PHONE, "");

        SpUtils.getInstance().remove(Constants.TIME_LOCATION);
        SpUtils.getInstance().remove(Constants.TIME_WEATHER);

        String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
        if (!TextUtils.isEmpty(saveVersion)) {

            //  注册
            WeatherApp.getInstance().startUsingApp();

            String versionName = DeviceUtils.getVersionName(this);
            if (!saveVersion.equals("0")) {
                LocationSpHelper.saveWithLocation(null);
                LocationHelper.getInstance().start(this);

                WeatherHttpHelper httpHelper = new WeatherHttpHelper(getApplicationContext());
                httpHelper.fetchCitiesWeather();
            }
            //  获取广告
            fetchSplashAD(this, container, skipView, getPosId(), this);
        } else {
            PermissionActivity.redirectTo(this);
        }
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
            next();
        }
        canJump = true;
    }

    @Override
    protected void onDestroy() {
        if (splashAD != null) {
            splashAD = null;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /** 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.skip_view) {
            onStartWeather();
        }
    }

    private void onStartWeather() {
        String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
        if (!TextUtils.isEmpty(saveVersion) && saveVersion.equals("0")) {
            CitySearchActivity.redirectTo(this, true);
        } else {
//            this.startActivity(new Intent(this, MainActivity.class));
            this.startActivity(new Intent(this, NewsActivity.class));
        }
        SpUtils.getInstance().putString(SpUtils.VERSION_APP, DeviceUtils.getVersionName(this));
        this.finish();
    }

    private void onShowSkip() {
        skipView = findViewById(R.id.skip_view);
        skipView.setVisibility(View.VISIBLE);
        skipView.setOnClickListener(this);
    }
}
