package com.goodtech.tq.ad;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.Glide;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.TToast;
import com.bytedance.msdk.api.nativeAd.TTNativeAdAppInfo;
import com.bytedance.msdk.api.nativeAd.TTViewBinder;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMAdDislike;
import com.bytedance.msdk.api.v2.GMAdSize;
import com.bytedance.msdk.api.v2.GMDislikeCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeCustomVideoReporter;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeExpressAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMVideoListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMViewBinder;
import com.goodtech.tq.R;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.cityList.CityListActivity;
import com.goodtech.tq.manager.AdFeedManager;
import com.goodtech.tq.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * com.goodtech.tq.ad
 */
public class AdFeedActivity extends BaseActivity {

    protected static final String TAG = "AdFeedActivity";

    /**
     * 展示原生广告
     */
    protected boolean showAd(FrameLayout feedContainer, AdFeedManager adFeedManager, boolean isLoaded, GMNativeAd gMNativeAd) {
        if (!isLoaded || adFeedManager == null || gMNativeAd == null) {
            //TToast.show(getContext(), "请先加载广告");
            // initNativeExpressAD();
            return false;
        }
        if (!gMNativeAd.isReady()) {
            //TToast.show(getContext(), "广告已经无效，请重新请求");
            // initNativeExpressAD();
            return false;
        }

        feedContainer.setVisibility(View.VISIBLE);
        View view = null;
        if (gMNativeAd.isExpressAd()) { //模板
            view = getExpressAdView(feedContainer, gMNativeAd);
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            //TToast.show(requireActivity(), "图片展示样式错误");
        }

        if (view != null) {
            view.setLayoutParams(new
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            feedContainer.removeAllViews();
            feedContainer.addView(view);
        }
        return true;
    }

    //渲染模板广告
    @SuppressWarnings("RedundantCast")
    protected View getExpressAdView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        final ExpressAdViewHolder adViewHolder;
        View convertView = null;
        try {
            convertView = LayoutInflater.from(this).inflate(R.layout.listitem_ad_native_express, parent, false);
            adViewHolder = new ExpressAdViewHolder();
            adViewHolder.mAdContainerView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_express);
            convertView.setTag(adViewHolder);

            //判断是否存在dislike按钮
            if (ad.hasDislike()) {
                ad.setDislikeCallback(this, new GMDislikeCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        //TToast.show(requireActivity(), "点击 " + value);
                        //用户选择不喜欢原因后，移除广告展示
                        removeAdView();
                    }

                    @Override
                    public void onCancel() {
                        //TToast.show(requireActivity(), "dislike 点击了取消");
                        Log.d(TAG, "dislike 点击了取消");
                    }

                    /**
                     * 拒绝再次提交
                     */
                    @Override
                    public void onRefuse() {

                    }

                    @Override
                    public void onShow() {

                    }
                });
            }

            //设置点击展示回调监听
            ad.setNativeAdListener(new GMNativeExpressAdListener() {
                @Override
                public void onAdClick() {
                    Log.d(TAG, "onAdClick");
                    //TToast.show(requireActivity(), "模板广告被点击");
                }

                @Override
                public void onAdShow() {
                    Log.d(TAG, "onAdShow");
                    //TToast.show(requireActivity(), "模板广告show");

                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    //TToast.show(requireActivity(), "模板广告渲染失败code=" + code + ",msg=" + msg);
                    Log.d(TAG, "onRenderFail   code=" + code + ",msg=" + msg);

                }

                // ** 注意点 ** 不要在广告加载成功回调里进行广告view展示，要在onRenderSucces进行广告view展示，否则会导致广告无法展示。
                @Override
                public void onRenderSuccess(float width, float height) {
                    Log.d(TAG, "onRenderSuccess");
                    //TToast.show(requireActivity(), "模板广告渲染成功:width=" + width + ",height=" + height);
                    //回调渲染成功后将模板布局添加的父View中
                    if (adViewHolder.mAdContainerView != null) {
                        //获取视频播放view,该view SDK内部渲染，在媒体平台可配置视频是否自动播放等设置。
                        int sWidth;
                        int sHeight;
                        /**
                         * 如果存在父布局，需要先从父布局中移除
                         */
                        final View video = ad.getExpressView(); // 获取广告view  如果存在父布局，需要先从父布局中移除
                        if (width == GMAdSize.FULL_WIDTH && height == GMAdSize.AUTO_HEIGHT) {
                            sWidth = FrameLayout.LayoutParams.MATCH_PARENT;
                            sHeight = FrameLayout.LayoutParams.WRAP_CONTENT;
                        } else {
                            sWidth = DeviceUtils.getScreenWidth(BaseApp.getInstance());
                            sHeight = (int) ((sWidth * height) / width);
                        }
                        if (video != null) {
                            /**
                             * 如果存在父布局，需要先从父布局中移除
                             */
                            DeviceUtils.removeFromParent(video);
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sWidth, sHeight);
                            adViewHolder.mAdContainerView.removeAllViews();
                            adViewHolder.mAdContainerView.addView(video, layoutParams);
                        }
                    }
                }
            });


            //视频广告设置播放状态回调（可选）
            ad.setVideoListener(new GMVideoListener() {

                @Override
                public void onVideoStart() {
                    //TToast.show(requireActivity(), "模板广告视频开始播放");
                    Log.d(TAG, "onVideoStart");
                }

                @Override
                public void onVideoPause() {
                    //TToast.show(requireActivity(), "模板广告视频暂停");
                    Log.d(TAG, "onVideoPause");

                }

                @Override
                public void onVideoResume() {
                    //TToast.show(requireActivity(), "模板广告视频继续播放");
                    Log.d(TAG, "onVideoResume");

                }

                @Override
                public void onVideoCompleted() {
                    //TToast.show(requireActivity(), "模板播放完成");
                    Log.d(TAG, "onVideoCompleted");
                }

                @Override
                public void onVideoError(AdError adError) {
                    //TToast.show(requireActivity(), "模板广告视频播放出错");
                    Log.d(TAG, "onVideoError");
                }

                @Override
                public void onProgressUpdate(long l, long l1) {

                }
            });

            ad.render();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    protected static class ExpressAdViewHolder {
        FrameLayout mAdContainerView;
    }

    protected void removeAdView() {
        
    }
}
