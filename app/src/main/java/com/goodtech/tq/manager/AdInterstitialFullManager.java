package com.goodtech.tq.manager;

import android.app.Activity;
import android.util.Log;

import com.bytedance.msdk.adapter.util.Logger;
import com.bytedance.msdk.api.GMAdEcpmInfo;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.GMSettingConfigCallback;
import com.bytedance.msdk.api.v2.ad.interstitialFull.GMInterstitialFullAd;
import com.bytedance.msdk.api.v2.ad.interstitialFull.GMInterstitialFullAdLoadCallback;
import com.bytedance.msdk.api.v2.slot.GMAdOptionUtil;
import com.bytedance.msdk.api.v2.slot.GMAdSlotInterstitialFull;
import com.goodtech.tq.R;
import com.goodtech.tq.app.BaseApp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插全屏管理类。
 * 只需要复制粘贴到项目中，通过回调处理相应的业务逻辑即可使用完成广告加载&展示
 */
public class AdInterstitialFullManager {
    private static final String TAG = "AdInterstitialFullManag";
    /**
     * 插全屏对应的广告对象
     * 每次加载全屏视频广告的时候需要新建一个GMInterstitialFullAd，否则可能会出现广告填充问题
     */
    private GMInterstitialFullAd mGMInterstitialFullAd;
    private Activity mActivity;
    /**
     * 插全屏加载广告回调
     * 请在加载广告成功后展示广告
     */
    private GMInterstitialFullAdLoadCallback mGMInterstitialFullAdLoadCallback;
    private String mAdUnitId; //广告位

    /**
     * ------------------------- 以下是必要实现，如果不实现会导致加载广告失败  --------------------------------------
     */

    /**
     * 管理类构造函数
     * @param activity 全屏展示的Activity
     * @param interstitialFullAdLoadCallback 插全屏加载广告回调
     */
    public AdInterstitialFullManager(Activity activity, GMInterstitialFullAdLoadCallback interstitialFullAdLoadCallback) {
        mActivity = activity;
        mGMInterstitialFullAdLoadCallback = interstitialFullAdLoadCallback;
    }

    /**
     * 获取插全屏广告对象
     */
    public GMInterstitialFullAd getGMInterstitialFullAd() {
        return mGMInterstitialFullAd;
    }

    /**
     * 加载插全屏广告，如果没有config配置会等到加载完config配置后才去请求广告
     * @param adUnitId  广告位ID
     */
    public void loadAdWithCallback(final String adUnitId) {
        this.mAdUnitId = adUnitId;

        if (GMMediationAdSdk.configLoadSuccess()) {
            loadAd(adUnitId);
        } else {
            GMMediationAdSdk.registerConfigCallback(mSettingConfigCallback);
        }
    }

    /**
     * 加载插全屏广告，如果没有config配置会等到加载完config配置后才去请求广告
     * @param adUnitId  广告位ID
     */
    private void loadAd(String adUnitId) {
        //Context 必须传activity
        mGMInterstitialFullAd = new GMInterstitialFullAd(mActivity, adUnitId);

        // Map<String, String> customData = new HashMap<>();
        // customData.put(GMAdConstant.CUSTOM_DATA_KEY_GDT, "gdt custom data");//目前仅支持gdt

        /**
         * 创建全屏广告请求类型参数GMAdSlotInterstitialFull,具体参数含义参考文档
         */
        GMAdSlotInterstitialFull adSlotInterstitialFull = new GMAdSlotInterstitialFull.Builder()
                // .setGMAdSlotBaiduOption(GMAdOptionUtil.getGMAdSlotBaiduOption().build())
                // .setGMAdSlotGDTOption(GMAdOptionUtil.getGMAdSlotGDTOption().build())
                .setImageAdSize(600, 600)  //设置宽高 （插全屏类型下_插屏广告使用）
                .setVolume(0.5f) //admob 声音配置，与setMuted配合使用
                .setUserID("user123")//用户id,必传参数 (插全屏类型下_全屏广告使用)
                // .setCustomData(customData)
                .setRewardName("金币") //奖励的名称
                .setRewardAmount(1)  //奖励的数量
                .setOrientation(GMAdConstant.HORIZONTAL)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL; (插全屏类型下_全屏广告使用)
                .setBidNotify(true)//开启bidding比价结果通知，默认值为false
                .build();

        mGMInterstitialFullAd.loadAd(adSlotInterstitialFull, mGMInterstitialFullAdLoadCallback);
    }

    /**
     * 在Activity onDestroy中需要调用清理资源
     */
    public void destroy() {
        if (mGMInterstitialFullAd != null) {
            mGMInterstitialFullAd.destroy();
        }
        mActivity = null;
        mGMInterstitialFullAdLoadCallback = null;
        GMMediationAdSdk.unregisterConfigCallback(mSettingConfigCallback); //注销config回调
    }

    /**
     * config配置回调
     */
    private GMSettingConfigCallback mSettingConfigCallback = new GMSettingConfigCallback() {
        @Override
        public void configLoad() {
            loadAd(mAdUnitId);
        }
    };


    /**
     * ------------------------- 以下是非必要功能请选择性使用  --------------------------------------
     */

    /**
     * 展示广告加载信息
     */
    public void printLoadAdInfo() {
        if (mGMInterstitialFullAd == null) {
            return;
        }
        /**
         * 获取已经加载的clientBidding ，多阶底价广告的相关信息
         */
        List<GMAdEcpmInfo> gmAdEcpmInfos = mGMInterstitialFullAd.getMultiBiddingEcpm();
        if (gmAdEcpmInfos != null) {
            for (GMAdEcpmInfo info : gmAdEcpmInfos) {
                Log.e(TAG, "***多阶+client相关信息*** AdNetworkPlatformId" + info.getAdNetworkPlatformId()
                        + "  AdNetworkRitId:" + info.getAdNetworkRitId()
                        + "  ReqBiddingType:" + info.getReqBiddingType()
                        + "  PreEcpm:" + info.getPreEcpm()
                        + "  LevelTag:" + info.getLevelTag()
                        + "  ErrorMsg:" + info.getErrorMsg()
                        + "  request_id:" + info.getRequestId()
                        + "  SdkName:" + info.getAdNetworkPlatformName()
                        + "  CustomSdkName:" + info.getCustomAdNetworkPlatformName());
            }
        }

        /**
         * 获取实时填充/缓存池中价格最优的代码位信息即相关价格信息，每次只有一个信息
         */
        GMAdEcpmInfo gmAdEcpmInfo = mGMInterstitialFullAd.getBestEcpm();
        if (gmAdEcpmInfo != null) {
            Log.e(TAG, "***实时填充/缓存池中价格最优的代码位信息*** AdNetworkPlatformId" + gmAdEcpmInfo.getAdNetworkPlatformId()
                    + "  AdNetworkRitId:" + gmAdEcpmInfo.getAdNetworkRitId()
                    + "  ReqBiddingType:" + gmAdEcpmInfo.getReqBiddingType()
                    + "  PreEcpm:" + gmAdEcpmInfo.getPreEcpm()
                    + "  LevelTag:" + gmAdEcpmInfo.getLevelTag()
                    + "  ErrorMsg:" + gmAdEcpmInfo.getErrorMsg()
                    + "  request_id:" + gmAdEcpmInfo.getRequestId()
                    + "  SdkName:" + gmAdEcpmInfo.getAdNetworkPlatformName()
                    + "  CustomSdkName:" + gmAdEcpmInfo.getCustomAdNetworkPlatformName());
        }

        /**
         * 获取获取当前缓存池的全部信息
         */
        List<GMAdEcpmInfo> gmCacheInfos = mGMInterstitialFullAd.getCacheList();
        if (gmCacheInfos != null) {
            for (GMAdEcpmInfo info : gmCacheInfos) {
                Log.e(TAG, "***缓存池的全部信息*** AdNetworkPlatformId" + info.getAdNetworkPlatformId()
                        + "  AdNetworkRitId:" + info.getAdNetworkRitId()
                        + "  ReqBiddingType:" + info.getReqBiddingType()
                        + "  PreEcpm:" + info.getPreEcpm()
                        + "  LevelTag:" + info.getLevelTag()
                        + "  ErrorMsg:" + info.getErrorMsg()
                        + "  request_id:" + info.getRequestId()
                        + "  SdkName:" + info.getAdNetworkPlatformName()
                        + "  CustomSdkName:" + info.getCustomAdNetworkPlatformName());
            }
        }
    }

    /**
     * 打印加载失败的adn错误信息
     */
    public void printLoadFailAdnInfo() {
        if (mGMInterstitialFullAd == null) {
            return;
        }

        // 获取本次waterfall加载中，加载失败的adn错误信息。
        Log.d(TAG, "InterstitialFull ad loadinfos: " + mGMInterstitialFullAd.getAdLoadInfoList());
    }

    /**
     * 打印已经展示的广告信息
     */
    public void printSHowAdInfo() {
        if (mGMInterstitialFullAd == null) {
            return;
        }
        GMAdEcpmInfo gmAdEcpmInfo = mGMInterstitialFullAd.getShowEcpm();
        if (gmAdEcpmInfo == null) {
            return;
        }
        String s = BaseApp.getInstance().getResources().getString(R.string.show_info,
                gmAdEcpmInfo.getAdNetworkRitId(),
                gmAdEcpmInfo.getAdnName(),
                gmAdEcpmInfo.getPreEcpm());
        Log.e(TAG, s);
    }

}
