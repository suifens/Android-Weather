package com.goodtech.tq;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.bytedance.msdk.adapter.util.Logger;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.AdSlot;
import com.bytedance.msdk.api.TTAdConstant;
import com.bytedance.msdk.api.UIUtils;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.GMNetworkPlatformConst;
import com.bytedance.msdk.api.v2.GMNetworkRequestInfo;
import com.bytedance.msdk.api.v2.GMPreloadRequestInfo;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAd;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdListener;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdLoadCallback;
import com.bytedance.msdk.api.v2.slot.GMAdOptionUtil;
import com.bytedance.msdk.api.v2.slot.GMAdSlotFullVideo;
import com.bytedance.msdk.api.v2.slot.GMAdSlotNative;
import com.bytedance.msdk.api.v2.slot.GMAdSlotRewardVideo;
import com.bytedance.msdk.api.v2.slot.GMAdSlotSplash;
import com.bytedance.msdk.api.v2.slot.paltform.GMAdSlotGDTOption;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.SplashUtils;
import com.goodtech.tq.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mSplashContainer = findViewById(R.id.splash_container);

        StatusBarUtil.setImmerseStatusBarSystemUiVisibility(this);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
        if (!TextUtils.isEmpty(saveVersion)) {
            //加载开屏广告
            mSplashContainer.post(this::loadSplashAd);

            if (LocationSpHelper.getCityListAndLocation().size() != 0) {
                //  注册
                BaseApp.getInstance().startUsingApp(this);

                SpUtils.getInstance().remove(Constants.TIME_LOCATION);
                SpUtils.getInstance().remove(Constants.TIME_WEATHER);
                if (!saveVersion.equals("0")) {
                    WeatherHttpHelper httpHelper = new WeatherHttpHelper(getApplicationContext());
                    httpHelper.getBaseUrl(httpHelper::fetchCitiesWeather);
                }

                SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false);
            }

        } else {
            handler.postDelayed(() -> {
                PermissionActivity.redirectTo(SplashActivity.this);
                this.finish();
            }, 500);
        }
    }

    /**
     * 跳转到主页面
     */
    private static final String EXTRA_BACK = "EXTRA_BACK";
    private void goToMainActivity() {
        // Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        // startActivity(intent);
        // overridePendingTransition(0, 0);

        if (!getIntent().getBooleanExtra(EXTRA_BACK, false)) {
            if (LocationSpHelper.getCityListAndLocation().size() == 0) {
                CitySearchActivity.redirectTo(this, true);
            } else {
                this.startActivity(new Intent(this, MainActivity.class));
            }
        }
        SpUtils.getInstance().putString(SpUtils.VERSION_APP, DeviceUtils.getVersionName(this));

        mSplashContainer.removeAllViews();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSplashContainer.removeAllViews();
    }

    // <editor-fold defaultstate="collapsed" desc="广告">
    private GMSplashAd mTTSplashAd;
    private FrameLayout mSplashContainer;

    //开屏广告加载超时时间,建议大于1000,这里为了冷启动第一次加载到广告并且展示,示例设置了2000ms
    private static final int AD_TIME_OUT = 3000;

    /**
     * 加载开屏广告
     */
    private void loadSplashAd() {
        //设置不支持小窗模式
        String mAdUnitId = Constants.PGE_SPLASH_POS_ID;
        // 注：每次加载开屏广告的时候需要新建一个TTSplashAd，否则可能会出现广告填充问题
        //  （ 例如：mTTSplashAd = new TTSplashAd(this, mAdUnitId);）
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
        GMNetworkRequestInfo networkRequestInfo = SplashUtils.getGMNetworkRequestInfo();


        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTSplashAd.loadAd(adSlot, networkRequestInfo, new GMSplashAdLoadCallback() {
            @Override
            public void onSplashAdLoadFail(AdError adError) {
                Log.d(TAG, adError.message);
                Log.e(TAG, "load splash ad error : " + adError.code + ", " + adError.message);
                goToMainActivity();

                // preloadAds(); // 预加载广告

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
                    Logger.e(TAG, "adNetworkPlatformId: " + mTTSplashAd.getAdNetworkPlatformId() + "   adNetworkRitId：" + mTTSplashAd.getAdNetworkRitId() + "   preEcpm: " + mTTSplashAd.getPreEcpm());
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
            Log.d(TAG, "onAdClicked");
        }

        @Override
        public void onAdShow() {
            Log.d(TAG, "onAdShow");
        }

        /**
         * show失败回调。如果show时发现无可用广告（比如广告过期），会触发该回调。
         * 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载。
         * @param adError showFail的具体原因
         */
        @Override
        public void onAdShowFail(AdError adError) {
            Log.d(TAG, "onAdShowFail");

            // 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载
            loadSplashAd();
        }

        @Override
        public void onAdSkip() {
            Log.d(TAG, "onAdSkip");

            goToMainActivity();

            // preloadAds(); // 预加载广告
        }

        @Override
        public void onAdDismiss() {
            Log.d(TAG, "onAdDismiss");
            goToMainActivity();

            // preloadAds(); // 预加载广告
        }
    };

    /**
     * 预加载说明：GroMore内部会根据开发者传入的广告位信息，并行数，时间间隔进行预请求，期间会产生较大的网络负载，因此建议开发者
     * 根据自己的情况进行预加载：
     * 1、如果app接入了开屏广告，建议在开屏广告展示结束后再触发预加载，以免增加开屏的加载耗时；
     * 2、如果没有接入开屏，则在MainActivity里进行预加载。
     */
    private void preloadAds() {
        // 示例为预加载激励视频、全屏视频、信息流和开屏

        // 准备开屏GMAdSlot
        GMAdSlotSplash gmAdSlotSplash = new GMAdSlotSplash.Builder()
                .setImageAdSize(UIUtils.getScreenWidth(this), UIUtils.getScreenHeight(this)) // 单位px
                .setSplashPreLoad(true)//开屏gdt开屏广告预加载
                .setMuted(false) //声音开启
                .setVolume(1f)//admob 声音配置，与setMuted配合使用
                .setTimeOut(AD_TIME_OUT)//设置超时
                .setSplashButtonType(TTAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN)
                .setDownloadType(TTAdConstant.DOWNLOAD_TYPE_POPUP)
                .setSplashShakeButton(true) //开屏摇一摇开关，默认开启，目前只有gdt支持
                .build();
        List<String> splashPrimeList = new ArrayList<>();
        splashPrimeList.add(Constants.PGE_SPLASH_POS_ID); // 可以添加多个信息流广告位
        GMPreloadRequestInfo splashRequestInfo = new GMPreloadRequestInfo(gmAdSlotSplash, splashPrimeList);

        // 准备激励视频GMAdSlot
        Map<String, String> customData = new HashMap<>();
        customData.put(GMAdConstant.CUSTOM_DATA_KEY_PANGLE, "pangle media_extra");
        // 如果开启了gromre服务端激励验证，可以传以下信息，跟adn无关。
        customData.put(GMAdConstant.CUSTOM_DATA_KEY_GROMORE_EXTRA, "gromore serverside verify extra data"); // 会透传给媒体的服务器

        GMAdSlotRewardVideo adSlotRewardVideo = new GMAdSlotRewardVideo.Builder()
                .setMuted(true)//对所有SDK的激励广告生效，除需要在平台配置的SDK，如穿山甲SDK
                .setVolume(0f)//配合Admob的声音大小设置[0-1]
                .setGMAdSlotGDTOption(GMAdOptionUtil.getGMAdSlotGDTOption().build())
                .setGMAdSlotBaiduOption(GMAdOptionUtil.getGMAdSlotBaiduOption().build())
                .setCustomData(customData)
                .setRewardName("金币") //奖励的名称
                .setRewardAmount(3)  //奖励的数量
                .setUserID("user123")//用户id,必传参数
                .setUseSurfaceView(false)
                .setOrientation(GMAdConstant.VERTICAL)//必填参数，期望视频的播放方向：GMAdConstant.HORIZONTAL 或 GMAdConstant.VERTICAL
                .setBidNotify(true)//开启bidding比价结果通知，默认值为false
                .build();
        // 需要预加载的广告位
        List<String> rewardVideoPrimeRitList = new ArrayList<>();
        rewardVideoPrimeRitList.add(Constants.PGE_INT_POS_ID); // 可以添加多个激励广告位
        GMPreloadRequestInfo rewardPreloadRequestInfo = new GMPreloadRequestInfo(adSlotRewardVideo, rewardVideoPrimeRitList);

        // 准备信息流GMAdslot
        // 针对Gdt Native自渲染广告，可以自定义gdt logo的布局参数。该参数可选,非必须。
        FrameLayout.LayoutParams gdtNativeAdLogoParams =
                new FrameLayout.LayoutParams(
                        SizeUtils.dp2px(40),
                        SizeUtils.dp2px(13),
                        Gravity.RIGHT | Gravity.TOP); // 例如，放在右上角
        GMAdSlotGDTOption.Builder adSlotNativeBuilder = GMAdOptionUtil.getGMAdSlotGDTOption()
                .setNativeAdLogoParams(gdtNativeAdLogoParams);

        GMAdSlotNative adSlotNative = new GMAdSlotNative.Builder()
                .setGMAdSlotBaiduOption(GMAdOptionUtil.getGMAdSlotBaiduOption().build())//百度相关的配置
                .setGMAdSlotGDTOption(adSlotNativeBuilder.build())//gdt相关的配置
                .setAdmobNativeAdOptions(GMAdOptionUtil.getAdmobNativeAdOptions())//admob相关配置
                .setAdStyleType(AdSlot.TYPE_EXPRESS_AD)//必传，表示请求的模板广告还是原生广告，AdSlot.TYPE_EXPRESS_AD：模板广告 ； AdSlot.TYPE_NATIVE_AD：原生广告
                // 备注
                // 1:如果是信息流自渲染广告，设置广告图片期望的图片宽高 ，不能为0
                // 2:如果是信息流模板广告，宽度设置为希望的宽度，高度设置为0(0为高度选择自适应参数)
                .setImageAdSize(ScreenUtils.getScreenWidth(), 340)// 必选参数 单位dp ，详情见上面备注解释
                .setAdCount(3)//请求广告数量为1到3条
                .setBidNotify(true)//开启bidding比价结果通知，默认值为false
                .build();
        List<String> expressFeedPrimeList = new ArrayList<>();
        expressFeedPrimeList.add("945493687"); // 可以添加多个信息流广告位
        GMPreloadRequestInfo expressFeedRequestInfo = new GMPreloadRequestInfo(adSlotNative, expressFeedPrimeList);

        List<GMPreloadRequestInfo> preloadRequestInfoList = new ArrayList<>();
        preloadRequestInfoList.add(splashRequestInfo);
        preloadRequestInfoList.add(rewardPreloadRequestInfo);
        preloadRequestInfoList.add(expressFeedRequestInfo);

        GMMediationAdSdk.preload(this, preloadRequestInfoList, 2, 2);
    }
    
    // </editor-fold>
}
