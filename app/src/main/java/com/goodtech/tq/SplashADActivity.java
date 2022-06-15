package com.goodtech.tq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bytedance.msdk.adapter.util.UIUtils;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.TTAdConstant;
import com.bytedance.msdk.api.v2.GMNetworkPlatformConst;
import com.bytedance.msdk.api.v2.GMNetworkRequestInfo;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAd;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdListener;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdLoadCallback;
import com.bytedance.msdk.api.v2.slot.GMAdSlotSplash;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.SplashUtils;
import com.goodtech.tq.utils.StatusBarUtil;
import com.qq.e.ads.splash.SplashAD;

/**
 * 这是demo工程的入口Activity，在这里会首次调用广点通的SDK。
 *
 * 在调用SDK之前，如果您的App的targetSDKVersion >= 23，那么建议动态申请相关权限。
 */
public class SplashADActivity extends Activity {

    private static final String TAG = "SplashADActivity";
    private SplashAD splashAD;
    private TextView skipView;
    private static final String SKIP_TEXT = "点击跳过 %d";
    private static final String EXTRA_BACK = "EXTRA_BACK";

    public boolean canJump = false;

    /**
     * 记录拉取广告的时间
     */
    private long fetchSplashADTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private GMSplashAd mTTSplashAd;
    private FrameLayout mSplashContainer;
    //是否强制跳转到主页面
    private boolean mForceGoMain;
    private String mAdUnitId = null;

    //开屏广告加载超时时间,建议大于1000,这里为了冷启动第一次加载到广告并且展示,示例设置了2000ms
    private static final int AD_TIME_OUT = 3000;
    private static final int MSG_GO_MAIN = 1;
    //开屏广告是否已经加载
    private boolean mHasLoaded;

    // 百度开屏广告点击跳转落地页后倒计时不暂停，即使在看落地页，倒计时结束后仍然会强制跳转，需要特殊处理：
    // 检测到广告被点击，且走了activity的onPaused证明跳转到了落地页，这时候onAdDismiss回调中不进行跳转，而是在activity的onResume中跳转。
    private boolean isBaiduSplashAd = false;
    private boolean baiduSplashAdClicked = false;
    private boolean onPaused = false;

    //----------------开屏小窗参数-------------------
    private boolean showInCurrent = false; //开屏小窗是否在当前页面展示

    public static void redirectTo(Activity ctx) {
        Intent intent = new Intent(ctx, SplashADActivity.class);
        ctx.startActivity(intent);
        ctx.overridePendingTransition(0, 0);
    }

    public static void redirectToFront(Activity ctx) {
        Intent intent = new Intent(ctx, SplashADActivity.class);
        intent.putExtra(EXTRA_BACK, true);
        ctx.startActivity(intent);
        ctx.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad);

        StatusBarUtil.setImmerseStatusBarSystemUiVisibility(this);
        mSplashContainer = this.findViewById(R.id.splash_container);

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mAdUnitId = Constants.PGE_SPLASH_POS_ID;
        //加载开屏广告
        mSplashContainer.post(new Runnable() {
            @Override
            public void run() {
                loadSplashAd();
            }
        });
    }

    /**
     * 加载开屏广告
     */
    private void loadSplashAd() {
        if (mAdUnitId == null) return;
        /**
         * 注：每次加载开屏广告的时候需要新建一个TTSplashAd，否则可能会出现广告填充问题
         * （ 例如：mTTSplashAd = new TTSplashAd(this, mAdUnitId);）
         */
        mTTSplashAd = new GMSplashAd(this, mAdUnitId);
        mTTSplashAd.setAdSplashListener(mSplashAdListener);

        //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
        GMAdSlotSplash adSlot = new GMAdSlotSplash.Builder()
                .setImageAdSize(UIUtils.getScreenWidth(this), UIUtils.getScreenHeight(this)) // 单位px
                .setSplashPreLoad(true)//开屏gdt开屏广告预加载
                .setMuted(false) //声音开启
                .setVolume(1f)//admob 声音配置，与setMuted配合使用
                .setTimeOut(AD_TIME_OUT)//设置超时
                .setSplashButtonType(TTAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN)
                .setDownloadType(TTAdConstant.DOWNLOAD_TYPE_POPUP)
                .setSplashShakeButton(true) //开屏摇一摇开关，默认开启，目前只有gdt支持
                .build();

        //自定义兜底方案 选择使用
        GMNetworkRequestInfo networkRequestInfo = SplashUtils.getGMNetworkRequestInfo(2);
        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTSplashAd.loadAd(adSlot, new GMSplashAdLoadCallback() {
            @Override
            public void onSplashAdLoadFail(com.bytedance.msdk.api.AdError adError) {
                Log.d(TAG, adError.message);
                mHasLoaded = true;
                Log.e(TAG, "load splash ad error : " + adError.code + ", " + adError.message);
                goToMainActivity();

                // 获取本次waterfall加载中，加载失败的adn错误信息。
                if (mTTSplashAd != null) {
                    Log.d(TAG, "ad load infos: " + mTTSplashAd.getAdLoadInfoList().toString());
                }
            }

            @Override
            public void onSplashAdLoadSuccess() {
                if (mTTSplashAd != null) {
                    if(mTTSplashAd.getAdNetworkPlatformId() == GMNetworkPlatformConst.SDK_NAME_KLEVIN){
                        //游可赢开屏与其他ADN开屏不同，是单独开启一个Activity，而不是通过传入的container添加，需要做符合业务的特殊处理
                    }
                    // 根据需要选择调用isReady()
//                    if (mTTSplashAd.isReady()) {
//                        mTTSplashAd.showAd(mSplashContainer);
//                    }
                    mTTSplashAd.showAd(mSplashContainer);
                    isBaiduSplashAd = mTTSplashAd.getAdNetworkPlatformId() == GMNetworkPlatformConst.SDK_NAME_BAIDU;
                    // 获取本次waterfall加载中，加载失败的adn错误信息。
                    Log.d(TAG, "ad load infos: " + mTTSplashAd.getAdLoadInfoList());
                }
                Log.e(TAG, "load splash ad success ");
            }

            // 注意：***** 开屏广告加载超时回调已废弃，统一走onSplashAdLoadFail，GroMore作为聚合不存在SplashTimeout情况。*****
            @Override
            public void onAdLoadTimeout() {
            }
        });

    }

    GMSplashAdListener mSplashAdListener = new GMSplashAdListener() {
        @Override
        public void onAdClicked() {
            baiduSplashAdClicked = true;
            showToast("开屏广告被点击");
            Log.d(TAG, "onAdClicked");
        }

        @Override
        public void onAdShow() {
            showToast("开屏广告展示");
            Log.d(TAG, "onAdShow");
        }

        /**
         * show失败回调。如果show时发现无可用广告（比如广告过期），会触发该回调。
         * 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载。
         * @param adError showFail的具体原因
         */
        @Override
        public void onAdShowFail(AdError adError) {
            showToast("开屏广告展示失败");
            Log.d(TAG, "onAdShowFail");

            // 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载
            loadSplashAd();
        }

        @Override
        public void onAdSkip() {
            showToast("开屏广告点击跳过按钮");
            Log.d(TAG, "onAdSkip");

            goToMainActivity();
        }

        @Override
        public void onAdDismiss() {
            showToast("开屏广告倒计时结束关闭");
            Log.d(TAG, "onAdDismiss");
            if (isBaiduSplashAd && onPaused && baiduSplashAdClicked) {
                // 这种情况下，百度开屏广告不能在onAdDismiss中跳转，需要在onResume中跳转主页。
                return;
            }
            goToMainActivity();
        }
    };

    @Override
    protected void onResume() {
        //判断是否该跳转到主页面
        if (mForceGoMain) {
            goToMainActivity();
        }
        if (isBaiduSplashAd && onPaused && baiduSplashAdClicked) {
            // 这种情况下，百度开屏广告不能在onAdDismiss中跳转，需要自己在onResume中跳转主页。
            goToMainActivity();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPaused = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mForceGoMain = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSplashContainer.removeAllViews();
        if (mTTSplashAd != null) {
           mTTSplashAd.destroy();  //跨页面展示开屏小窗时不能对相关广告进行销毁
        }
    }

    /**
     * 跳转到主页面
     */
    private void goToMainActivity() {
        // Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        // startActivity(intent);
        // overridePendingTransition(0, 0);
        // mSplashContainer.removeAllViews();
        // this.finish();
        onStartWeather();
    }

    private void showToast(String msg) {
        //TToast.show(this, msg);
    }


    /** 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onStartWeather() {

        if (!getIntent().getBooleanExtra(EXTRA_BACK, false)) {
            String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
            if (!TextUtils.isEmpty(saveVersion) && saveVersion.equals("0")
                    || LocationSpHelper.getCityListAndLocation().size() == 0) {
                CitySearchActivity.redirectTo(this, true);
            } else {
                this.startActivity(new Intent(this, MainActivity.class));
            }
        }
        SpUtils.getInstance().putString(SpUtils.VERSION_APP, DeviceUtils.getVersionName(this));
        this.finish();
    }

    private void onShowSkip() {
        skipView = findViewById(R.id.skip_view);
        skipView.setVisibility(View.VISIBLE);
        // skipView.setOnClickListener(this);
    }
}
