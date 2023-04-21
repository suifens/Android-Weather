package com.goodtech.tq.manager;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.bytedance.msdk.api.GMAdEcpmInfo;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.GMSettingConfigCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdLoadCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMUnifiedNativeAd;
import com.bytedance.msdk.api.v2.slot.GMAdOptionUtil;
import com.bytedance.msdk.api.v2.slot.GMAdSlotNative;
import com.bytedance.msdk.api.v2.slot.paltform.GMAdSlotGDTOption;
import com.goodtech.tq.utils.DeviceUtils;

import java.util.List;

/**
 * 信息流管理类。
 * 只需要复制粘贴到项目中，通过回调处理相应的业务逻辑即可使用完成广告加载&展示
 */
public class AdFeedManager {
    private static final String TAG = "AdFeedManager";

    /**
     * 信息流对应的广告对象
     * 每次加载信息流广告的时候需要新建一个GMUnifiedNativeAd，否则可能会出现广告填充问题
     */
    private GMUnifiedNativeAd mGMUnifiedNativeAd;
    private Activity mActivity;
    /**
     * 信息流加载广告回调
     * 请在加载广告成功后展示广告
     */
    private GMNativeAdLoadCallback mGMNativeAdLoadCallback;
    private String mAdUnitId; //广告位
    private int mAdCount; //广告数量
    private int mStyleType; //模板类型，可以不传。以服务端类型为准
    private int mAdWidth;

    /**
     * ------------------------- 以下是必要实现，如果不实现会导致加载广告失败  --------------------------------------
     */

    /**
     * 管理类构造函数
     * @param activity 信息流展示的Activity
     * @param nativeAdLoadCallback 信息流加载广告回调
     */
    public AdFeedManager(Activity activity, GMNativeAdLoadCallback nativeAdLoadCallback) {
        mActivity = activity;
        mGMNativeAdLoadCallback = nativeAdLoadCallback;
    }

    /**
     * 获取信息流广告对象
     */
    public GMUnifiedNativeAd getGMUnifiedNativeAd() {
        return mGMUnifiedNativeAd;
    }


    /**
     * 加载信息流广告，如果没有config配置会等到加载完config配置后才去请求广告
     * @param adUnitId 广告位ID
     * @param adCount 广告数量
     * @param styleType 模板类型
     */
    public void loadAdWithCallback(final String adUnitId, int adCount, int styleType, int adWidth) {
        this.mAdWidth = adWidth;
        this.mAdUnitId = adUnitId;
        this.mAdCount = adCount;
        this.mStyleType = styleType;

        if (GMMediationAdSdk.configLoadSuccess()) {
            loadAd(adUnitId, adCount, styleType, adWidth);
        } else {
            GMMediationAdSdk.registerConfigCallback(mSettingConfigCallback); //不用使用内部类，否则在ondestory中无法移除该回调
        }
    }

    /**
     * 真正的开始加载信息流广告
     * @param adUnitId 广告位ID
     * @param adCount 广告数量
     * @param styleType 模板类型
     */
    private void loadAd(String adUnitId, int adCount, int styleType, int adWidth) {
        mGMUnifiedNativeAd = new GMUnifiedNativeAd(mActivity, adUnitId);//模板视频

        // 针对Gdt Native自渲染广告，可以自定义gdt logo的布局参数。该参数可选,非必须。
        FrameLayout.LayoutParams gdtNativeAdLogoParams =
                new FrameLayout.LayoutParams(
                        // DeviceUtils.dip2px(mActivity.getApplicationContext(), 40),
                        // DeviceUtils.dip2px(mActivity.getApplicationContext(), 13),
                        0, 0,
                        Gravity.TOP|Gravity.START); // 例如，放在右上角


        GMAdSlotGDTOption.Builder adSlotNativeBuilder = GMAdOptionUtil.getGMAdSlotGDTOption()
                .setNativeAdLogoParams(gdtNativeAdLogoParams);

        /**
         * 创建feed广告请求类型参数GMAdSlotNative,具体参数含义参考文档
         */
        GMAdSlotNative adSlotNative = new GMAdSlotNative.Builder()
                .setGMAdSlotBaiduOption(GMAdOptionUtil.getGMAdSlotBaiduOption().build())//百度相关的配置
                .setGMAdSlotGDTOption(adSlotNativeBuilder.build())//gdt相关的配置
                .setAdmobNativeAdOptions(GMAdOptionUtil.getAdmobNativeAdOptions())//admob相关配置
                // .setAdStyleType(styleType)//必传，表示请求的模板广告还是原生广告，AdSlot.TYPE_EXPRESS_AD：模板广告 ； AdSlot.TYPE_NATIVE_AD：原生广告
                // 备注
                // 1:如果是信息流自渲染广告，设置广告图片期望的图片宽高 ，不能为0
                // 2:如果是信息流模板广告，宽度设置为希望的宽度，高度设置为0(0为高度选择自适应参数)
                .setImageAdSize(adWidth, 0)// 必选参数 单位dp ，详情见上面备注解释
                .setAdCount(adCount)//请求广告数量为1到3条
                .setBidNotify(true)//开启bidding比价结果通知，默认值为false
                .build();

        mGMUnifiedNativeAd.loadAd(adSlotNative, mGMNativeAdLoadCallback);
    }

    /**
     * 在Activity onDestroy中需要调用清理资源
     */
    public void destroy() {
        if (mGMUnifiedNativeAd != null) {
            mGMUnifiedNativeAd.destroy();
        }
        mActivity = null;
        mGMNativeAdLoadCallback = null;
        GMMediationAdSdk.unregisterConfigCallback(mSettingConfigCallback); //注销config回调
    }

    /**
     * config配置回调
     */
    private GMSettingConfigCallback mSettingConfigCallback = new GMSettingConfigCallback() {
        @Override
        public void configLoad() {
            loadAd(mAdUnitId, mAdCount, mStyleType, mAdWidth);
        }
    };

}
