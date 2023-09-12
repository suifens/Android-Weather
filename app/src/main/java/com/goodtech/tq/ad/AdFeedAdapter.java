package com.goodtech.tq.ad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.GMAdEcpmInfo;
import com.bytedance.msdk.api.TToast;
import com.bytedance.msdk.api.UIUtils;
import com.bytedance.msdk.api.nativeAd.TTNativeAdAppInfo;
import com.bytedance.msdk.api.nativeAd.TTViewBinder;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMAdDislike;
import com.bytedance.msdk.api.v2.GMAdSize;
import com.bytedance.msdk.api.v2.GMDislikeCallback;
import com.bytedance.msdk.api.v2.ad.GMAdAppDownloadListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeExpressAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMVideoListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMViewBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdFeedAdapter extends BaseAdapter {
    private static final String TAG = "Feed";

    //--------------------信息流内容-------------------------
    private static final int ITEM_VIEW_TYPE_NORMAL = 0; //信息流内容

    //--------------------信息流模板广告-------------------------
    private static final int ITEM_VIEW_TYPE_EXPRESS_AD = 6;//模板

    //--------------------信息流自渲染广告-------------------------
    private static final int ITEM_VIEW_TYPE_GROUP_PIC_AD = 1;   //自渲染：组图广告
    private static final int ITEM_VIEW_TYPE_SMALL_PIC_AD = 2;   //自渲染：小图广告
    private static final int ITEM_VIEW_TYPE_LARGE_PIC_AD = 3;   //自渲染：大图广告
    private static final int ITEM_VIEW_TYPE_VIDEO = 4;   //自渲染：视频广告
    private static final int ITEM_VIEW_TYPE_VIDEO_VERTICAL = 7; //自渲染：竖版视频
    private static final int ITEM_VIEW_TYPE_VERTICAL_IMG = 5;//自渲染：竖版图片

    private List<GMNativeAd> mData; //广告对象
    private Context mContext;
    private DislikeCallBack mCallBack;

    public AdFeedAdapter(Context context, List<GMNativeAd> data, DislikeCallBack callBack) {
        this.mContext = context;
        this.mData = data;
        this.mCallBack = callBack;
    }

    @Override
    public int getCount() {
        return mData.size(); // for test
    }

    @Override
    public GMNativeAd getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //信息流广告的样式
    // 1. 通过ad.isExpressAd()判断是否是模板广告
    // 2. 自渲染广告：通过ad.getImageMode()来判断 大图、小图、组图和视频广告
    @Override
    public int getItemViewType(int position) {
        GMNativeAd ad = getItem(position);

        //---------------非广告：信息流内容--------------------
        if (ad == null) {  //非广告：信息流内容
            return ITEM_VIEW_TYPE_NORMAL;
        }

        //---------------信息流模板广告--------------------
        if (ad != null && ad.isExpressAd()) { //信息流模板广告
            return ITEM_VIEW_TYPE_EXPRESS_AD;
        }

        //---------------信息流自渲染广告--------------------
        if (ad.getAdImageMode() == GMAdConstant.IMAGE_MODE_SMALL_IMG) { //信息流自渲染广告 ： 小图广告
            return ITEM_VIEW_TYPE_SMALL_PIC_AD;
        } else if (ad.getAdImageMode() == GMAdConstant.IMAGE_MODE_LARGE_IMG) {//信息流自渲染广告 ： 大图广告
            return ITEM_VIEW_TYPE_LARGE_PIC_AD;
        } else if (ad.getAdImageMode() == GMAdConstant.IMAGE_MODE_GROUP_IMG) {//信息流自渲染广告 ： 组图广告
            return ITEM_VIEW_TYPE_GROUP_PIC_AD;
        } else if (ad.getAdImageMode() == GMAdConstant.IMAGE_MODE_VIDEO) {//信息流自渲染广告 ： 视频广告
            return ITEM_VIEW_TYPE_VIDEO;
        } else if (ad.getAdImageMode() == GMAdConstant.IMAGE_MODE_VERTICAL_IMG) {//信息流自渲染广告 ： 竖图广告
            return ITEM_VIEW_TYPE_VERTICAL_IMG;
        } else if (ad.getAdImageMode() == GMAdConstant.IMAGE_MODE_VIDEO_VERTICAL) {//信息流自渲染广告 ： 竖版视频广告
            return ITEM_VIEW_TYPE_VIDEO_VERTICAL;
        }

        //---------------非广告：信息流内容--------------------
        TToast.show(mContext, "图片展示样式错误");
        return ITEM_VIEW_TYPE_NORMAL;

    }

    @Override
    public int getViewTypeCount() {
        return 8;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GMNativeAd ad = getItem(position);
        if (getItemViewType(position) != ITEM_VIEW_TYPE_NORMAL && ad != null && !ad.isReady()) {
            return getNormalView(convertView, parent, position, "广告已经无效，请重新加载");
        }

        switch (getItemViewType(position)) {
            //----------------信息流模板广告渲染----------------
            case ITEM_VIEW_TYPE_EXPRESS_AD:
                return getExpressAdView(convertView, parent, ad);

            //----------------信息流自渲染广告渲染----------------
            case ITEM_VIEW_TYPE_SMALL_PIC_AD:
                return getSmallAdView(convertView, parent, ad); //信息流自渲染广告渲染 ： 小图广告渲染
            case ITEM_VIEW_TYPE_LARGE_PIC_AD:
                return getLargeAdView(convertView, parent, ad); //信息流自渲染广告渲染 ： 大图广告渲染
            case ITEM_VIEW_TYPE_GROUP_PIC_AD:
                return getGroupAdView(convertView, parent, ad); //信息流自渲染广告渲染 ： 组图广告渲染
            case ITEM_VIEW_TYPE_VIDEO:
            case ITEM_VIEW_TYPE_VIDEO_VERTICAL:
                return getVideoView(convertView, parent, ad); //信息流自渲染广告渲染 ： 视频广告渲染
            case ITEM_VIEW_TYPE_VERTICAL_IMG:
                return getVerticalAdView(convertView, parent, ad); //信息流自渲染广告渲染 ： 竖图广告渲染

            //----------------信息流内容渲染----------------
            default:
                return getNormalView(convertView, parent, position, "");
        }
    }

    /**
     * 信息流模板广告渲染
     */
    @SuppressWarnings("RedundantCast")
    private View getExpressAdView(View convertView, ViewGroup parent, @NonNull final GMNativeAd ad) {
        final ExpressAdViewHolder adViewHolder;
        try {
            if (convertView == null || !(convertView.getTag() instanceof ExpressAdViewHolder)) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_native_express, parent, false);
                adViewHolder = new ExpressAdViewHolder();
                adViewHolder.mAdContainerView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_express);
                convertView.setTag(adViewHolder);
            } else {
                adViewHolder = (ExpressAdViewHolder) convertView.getTag();
            }

            //判断是否存在dislike按钮
            if (ad.hasDislike()) {
                ad.setDislikeCallback((Activity) mContext, new GMDislikeCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        TToast.show(mContext, "点击 " + value);
                        //用户选择不喜欢原因后，移除广告展示
                        mData.remove(ad);
                        mCallBack.dislikeClick();
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancel() {
                        TToast.show(mContext, "dislike 点击了取消");
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
                    TToast.show(mContext, "模板广告被点击");
                }

                @Override
                public void onAdShow() {
                    Log.d(TAG, "onAdShow_模板广告");
                    TToast.show(mContext, "模板广告show");
                    if (ad != null) {
                        GMAdEcpmInfo gmAdEcpmInfo = ad.getShowEcpm();
                        if (gmAdEcpmInfo != null) {
                            Log.e(AppConst.TAG, "adNetworkPlatformName: " + gmAdEcpmInfo.getAdNetworkPlatformName() + "   adNetworkRitId：" + gmAdEcpmInfo.getAdNetworkRitId() + "   preEcpm: " + gmAdEcpmInfo.getPreEcpm());
                        }
                    }
                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    TToast.show(mContext, "模板广告渲染失败code=" + code + ",msg=" + msg);
                    Log.d(TAG, "onRenderFail   code=" + code + ",msg=" + msg);

                }

                // ** 注意点 ** 不要在广告加载成功回调里进行广告view展示，要在onRenderSucces进行广告view展示，否则会导致广告无法展示。
                @Override
                public void onRenderSuccess(float width, float height) {
                    Log.d(TAG, "onRenderSuccess");
                    TToast.show(mContext, "模板广告渲染成功:width=" + width + ",height=" + height);
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
                            sWidth = UIUtils.getScreenWidth(mContext);
                            sHeight = (int) ((sWidth * height) / width);
                        }
                        if (video != null) {
                            /**
                             * 如果存在父布局，需要先从父布局中移除
                             */
                            UIUtils.removeFromParent(video);
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
                    TToast.show(mContext, "模板广告视频开始播放");
                    Log.d(TAG, "onVideoStart");
                }

                @Override
                public void onVideoPause() {
                    TToast.show(mContext, "模板广告视频暂停");
                    Log.d(TAG, "onVideoPause");

                }

                @Override
                public void onVideoResume() {
                    TToast.show(mContext, "模板广告视频继续播放");
                    Log.d(TAG, "onVideoResume");

                }

                @Override
                public void onVideoCompleted() {
                    TToast.show(mContext, "模板播放完成");
                    Log.d(TAG, "onVideoCompleted");
                }

                @Override
                public void onVideoError(AdError adError) {
                    TToast.show(mContext, "模板广告视频播放出错");
                    Log.d(TAG, "onVideoError");
                }

                @Override
                public void onProgressUpdate(long current, long duration) {
                    TToast.show(mContext, "模板广告视频播放进度");
//                        Log.d(TAG, "onProgressUpdate");
                }
            });

            ad.render();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


    /**
     * 信息流自渲染广告渲染 ：竖图广告渲染
     */
    private View getVerticalAdView(View convertView, ViewGroup parent, @NonNull final GMNativeAd ad) {
        VerticalAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        if (convertView == null || !(convertView.getTag() instanceof VerticalAdViewHolder)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_vertical_pic, parent, false);
            adViewHolder = new VerticalAdViewHolder();
            adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
            adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
            adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
            adViewHolder.mVerticalImage = convertView.findViewById(R.id.iv_listitem_image);
            adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
            adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
            adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
            adViewHolder.mLogo = convertView.findViewById(R.id.tt_ad_logo);//logoView 建议传入GroupView类型

            adViewHolder.app_info = convertView.findViewById(R.id.app_info);
            adViewHolder.app_name = convertView.findViewById(R.id.app_name);
            adViewHolder.author_name = convertView.findViewById(R.id.author_name);
            adViewHolder.package_size = convertView.findViewById(R.id.package_size);
            adViewHolder.permissions_url = convertView.findViewById(R.id.permissions_url);
            adViewHolder.permissions_content = convertView.findViewById(R.id.permissions_content);
            adViewHolder.privacy_agreement = convertView.findViewById(R.id.privacy_agreement);
            adViewHolder.version_name = convertView.findViewById(R.id.version_name);

            viewBinder = new GMViewBinder.Builder(R.layout.listitem_ad_vertical_pic)
                    .titleId(R.id.tv_listitem_ad_title)
                    .descriptionTextId(R.id.tv_listitem_ad_desc)
                    .mainImageId(R.id.iv_listitem_image)
                    .iconImageId(R.id.iv_listitem_icon)
                    .callToActionId(R.id.btn_listitem_creative)
                    .sourceId(R.id.tv_listitem_ad_source)
                    .logoLayoutId(R.id.tt_ad_logo)//logoView 建议传入GroupView类型
                    .build();
            adViewHolder.viewBinder = viewBinder;
            convertView.setTag(adViewHolder);
        } else {
            adViewHolder = (VerticalAdViewHolder) convertView.getTag();
            viewBinder = adViewHolder.viewBinder;
        }
        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageUrl() != null) {
            Glide.with(mContext).load(ad.getImageUrl()).into(adViewHolder.mVerticalImage);
        }

        return convertView;
    }

    /**
     * 信息流自渲染广告渲染 ：视频广告渲染
     */
    @SuppressWarnings("RedundantCast")
    private View getVideoView(View convertView, ViewGroup parent, @NonNull final GMNativeAd ad) {
        final VideoAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        try {
            if (convertView == null || !(convertView.getTag() instanceof VideoAdViewHolder)) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_large_video, parent, false);
                adViewHolder = new VideoAdViewHolder();
                adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
                adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
                adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
                adViewHolder.videoView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_video);
                // 可以通过GMNativeAd.getVideoWidth()、GMNativeAd.getVideoHeight()来获取视频的尺寸，进行UI调整（如果有需求的话）。
                // 在使用时需要判断返回值，如果返回为0，即表示该adn的广告不支持。目前仅Pangle和ks支持。
//                    int videoWidth = ad.getVideoWidth();
//                    int videoHeight = ad.getVideoHeight();
                adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
                adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
                adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
                adViewHolder.mLogo = convertView.findViewById(R.id.tt_ad_logo);//logoView 建议传入GroupView类型

                adViewHolder.app_info = convertView.findViewById(R.id.app_info);
                adViewHolder.app_name = convertView.findViewById(R.id.app_name);
                adViewHolder.author_name = convertView.findViewById(R.id.author_name);
                adViewHolder.package_size = convertView.findViewById(R.id.package_size);
                adViewHolder.permissions_url = convertView.findViewById(R.id.permissions_url);
                adViewHolder.permissions_content = convertView.findViewById(R.id.permissions_content);
                adViewHolder.privacy_agreement = convertView.findViewById(R.id.privacy_agreement);
                adViewHolder.version_name = convertView.findViewById(R.id.version_name);

                //TTViewBinder 是必须类,需要开发者在确定好View之后把Id设置给TTViewBinder类，并在注册事件时传递给SDK
                viewBinder = new GMViewBinder.Builder(R.layout.listitem_ad_large_video).
                        titleId(R.id.tv_listitem_ad_title).
                        sourceId(R.id.tv_listitem_ad_source).
                        descriptionTextId(R.id.tv_listitem_ad_desc).
                        mediaViewIdId(R.id.iv_listitem_video).
                        callToActionId(R.id.btn_listitem_creative).
                        logoLayoutId(R.id.tt_ad_logo).//logoView 建议传入GroupView类型
                        iconImageId(R.id.iv_listitem_icon).build();
                adViewHolder.viewBinder = viewBinder;
                convertView.setTag(adViewHolder);
            } else {
                adViewHolder = (VideoAdViewHolder) convertView.getTag();
                viewBinder = adViewHolder.viewBinder;
            }

            //视频广告设置播放状态回调（可选）
            ad.setVideoListener(new GMVideoListener() {

                @Override
                public void onVideoStart() {
                    TToast.show(mContext, "广告视频开始播放");
                    Log.d(TAG, "onVideoStart");
                }

                @Override
                public void onVideoPause() {
                    TToast.show(mContext, "广告视频暂停");
                    Log.d(TAG, "onVideoPause");
                }

                @Override
                public void onVideoResume() {
                    TToast.show(mContext, "广告视频继续播放");
                    Log.d(TAG, "onVideoResume");
                }

                @Override
                public void onVideoCompleted() {
                    TToast.show(mContext, "广告播放完成");
                    Log.d(TAG, "onVideoCompleted");
                }

                @Override
                public void onVideoError(AdError adError) {
                    TToast.show(mContext, "广告视频播放出错");
                    Log.d(TAG, "onVideoError");
                }

                @Override
                public void onProgressUpdate(long current, long duration) {
                    TToast.show(mContext, "广告视频播放进度");
//                        Log.d(TAG, "onProgressUpdate");
                }
            });

            //广告下载事件监听，仅下载类广告生效，可选
            ad.setAppDownloadListener(new GMAdAppDownloadListener() {
                @Override
                public void onIdle() {
                    TToast.show(mContext, "未开始下载");
                    Log.d(TAG, "onIdle");
                }

                @Override
                public void onDownloadStarted() {
                    TToast.show(mContext, "开始下载");
                    Log.d(TAG, "onDownloadStarted");
                }

                @Override
                public void onDownloadProgress(long totalBytes, long currBytes, int progress, int adnType) {
                    TToast.show(mContext, "下载中回调");
                    //Log.d(TAG, "onDownloadActive");
                }


                @Override
                public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                    TToast.show(mContext, "下载暂停回调");
                    Log.d(TAG, "onDownloadPaused");
                }

                @Override
                public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                    TToast.show(mContext, "下载失败回调");
                    Log.d(TAG, "onDownloadFailed");
                }

                @Override
                public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                    TToast.show(mContext, "安装完成回调");
                    Log.d(TAG, "onDownloadFinished");
                }

                @Override
                public void onInstalled(String fileName, String appName) {
                    TToast.show(mContext, "下载完成回调");
                    Log.d(TAG, "onInstalled");
                }
            });

            //绑定广告数据、设置交互回调
            bindData(convertView, adViewHolder, ad, viewBinder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    /**
     * 信息流自渲染广告渲染 ：大图广告渲染
     */
    @SuppressWarnings("RedundantCast")
    private View getLargeAdView(View convertView, ViewGroup parent, @NonNull final GMNativeAd ad) {
        final LargeAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        if (convertView == null || !(convertView.getTag() instanceof LargeAdViewHolder)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_large_pic, parent, false);
            adViewHolder = new LargeAdViewHolder();
            adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
            adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
            adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
            adViewHolder.mLargeImage = (ImageView) convertView.findViewById(R.id.iv_listitem_image);
            adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
            adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
            adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
            adViewHolder.mLogo = convertView.findViewById(R.id.tt_ad_logo);//logoView 建议传入GroupView类型

            adViewHolder.app_info = convertView.findViewById(R.id.app_info);
            adViewHolder.app_name = convertView.findViewById(R.id.app_name);
            adViewHolder.author_name = convertView.findViewById(R.id.author_name);
            adViewHolder.package_size = convertView.findViewById(R.id.package_size);
            adViewHolder.permissions_url = convertView.findViewById(R.id.permissions_url);
            adViewHolder.permissions_content = convertView.findViewById(R.id.permissions_content);
            adViewHolder.privacy_agreement = convertView.findViewById(R.id.privacy_agreement);
            adViewHolder.version_name = convertView.findViewById(R.id.version_name);

            viewBinder = new GMViewBinder.Builder(R.layout.listitem_ad_large_pic).
                    titleId(R.id.tv_listitem_ad_title).
                    descriptionTextId(R.id.tv_listitem_ad_desc).
                    sourceId(R.id.tv_listitem_ad_source).
                    mainImageId(R.id.iv_listitem_image).
                    callToActionId(R.id.btn_listitem_creative).
                    logoLayoutId(R.id.tt_ad_logo).//logoView 建议传入GroupView类型
                    iconImageId(R.id.iv_listitem_icon).build();
            adViewHolder.viewBinder = viewBinder;
            convertView.setTag(adViewHolder);
        } else {
            adViewHolder = (LargeAdViewHolder) convertView.getTag();
            viewBinder = adViewHolder.viewBinder;
        }
        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageUrl() != null) {
            Glide.with(mContext).load(ad.getImageUrl()).into(adViewHolder.mLargeImage);
        }
        return convertView;
    }

    /**
     * 信息流自渲染广告渲染 ：组图广告渲染
     */
    @SuppressWarnings("RedundantCast")
    private View getGroupAdView(View convertView, ViewGroup parent, @NonNull final GMNativeAd ad) {
        GroupAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        if (convertView == null || !(convertView.getTag() instanceof GroupAdViewHolder)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_group_pic, parent, false);
            adViewHolder = new GroupAdViewHolder();
            adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
            adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
            adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
            adViewHolder.mGroupImage1 = (ImageView) convertView.findViewById(R.id.iv_listitem_image1);
            adViewHolder.mGroupImage2 = (ImageView) convertView.findViewById(R.id.iv_listitem_image2);
            adViewHolder.mGroupImage3 = (ImageView) convertView.findViewById(R.id.iv_listitem_image3);
            adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
            adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
            adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
            adViewHolder.mLogo = convertView.findViewById(R.id.tt_ad_logo);//logoView 建议传入GroupView类型

            adViewHolder.app_info = convertView.findViewById(R.id.app_info);
            adViewHolder.app_name = convertView.findViewById(R.id.app_name);
            adViewHolder.author_name = convertView.findViewById(R.id.author_name);
            adViewHolder.package_size = convertView.findViewById(R.id.package_size);
            adViewHolder.permissions_url = convertView.findViewById(R.id.permissions_url);
            adViewHolder.permissions_content = convertView.findViewById(R.id.permissions_content);
            adViewHolder.privacy_agreement = convertView.findViewById(R.id.privacy_agreement);
            adViewHolder.version_name = convertView.findViewById(R.id.version_name);

            viewBinder = new TTViewBinder.Builder(R.layout.listitem_ad_group_pic).
                    titleId(R.id.tv_listitem_ad_title).
                    descriptionTextId(R.id.tv_listitem_ad_desc).
                    sourceId(R.id.tv_listitem_ad_source).
                    mainImageId(R.id.iv_listitem_image1).//传第一张即可
                    logoLayoutId(R.id.tt_ad_logo).//logoView 建议传入GroupView类型
                    callToActionId(R.id.btn_listitem_creative).
                    iconImageId(R.id.iv_listitem_icon).
                    groupImage1Id(R.id.iv_listitem_image1).
                    groupImage2Id(R.id.iv_listitem_image2).
                    groupImage3Id(R.id.iv_listitem_image3).
                    build();
            adViewHolder.viewBinder = viewBinder;
            convertView.setTag(adViewHolder);
        } else {
            adViewHolder = (GroupAdViewHolder) convertView.getTag();
            viewBinder = adViewHolder.viewBinder;
        }

        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageList() != null && ad.getImageList().size() >= 3) {
            String image1 = ad.getImageList().get(0);
            String image2 = ad.getImageList().get(1);
            String image3 = ad.getImageList().get(2);
            if (image1 != null) {
                Glide.with(mContext).load(image1).into(adViewHolder.mGroupImage1);
            }
            if (image2 != null) {
                Glide.with(mContext).load(image2).into(adViewHolder.mGroupImage2);
            }
            if (image3 != null) {
                Glide.with(mContext).load(image3).into(adViewHolder.mGroupImage3);
            }
        }
        return convertView;
    }


    /**
     * 信息流自渲染广告渲染 ：小图广告渲染
     */
    @SuppressWarnings("RedundantCast")
    private View getSmallAdView(View convertView, ViewGroup parent, @NonNull final GMNativeAd ad) {
        SmallAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        if (convertView == null || !(convertView.getTag() instanceof SmallAdViewHolder)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_small_pic, parent, false);
            adViewHolder = new SmallAdViewHolder();
            adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
            adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
            adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
            adViewHolder.mSmallImage = (ImageView) convertView.findViewById(R.id.iv_listitem_image);
            adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
            adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
            adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);

            adViewHolder.app_info = convertView.findViewById(R.id.app_info);
            adViewHolder.app_name = convertView.findViewById(R.id.app_name);
            adViewHolder.author_name = convertView.findViewById(R.id.author_name);
            adViewHolder.package_size = convertView.findViewById(R.id.package_size);
            adViewHolder.permissions_url = convertView.findViewById(R.id.permissions_url);
            adViewHolder.permissions_content = convertView.findViewById(R.id.permissions_content);
            adViewHolder.privacy_agreement = convertView.findViewById(R.id.privacy_agreement);
            adViewHolder.version_name = convertView.findViewById(R.id.version_name);

            viewBinder = new GMViewBinder.Builder(R.layout.listitem_ad_small_pic).
                    titleId(R.id.tv_listitem_ad_title).
                    sourceId(R.id.tv_listitem_ad_source).
                    descriptionTextId(R.id.tv_listitem_ad_desc).
                    mainImageId(R.id.iv_listitem_image).
                    logoLayoutId(R.id.tt_ad_logo).//logoView 建议为GroupView 类型
                    callToActionId(R.id.btn_listitem_creative).
                    iconImageId(R.id.iv_listitem_icon).build();
            adViewHolder.viewBinder = viewBinder;
            convertView.setTag(adViewHolder);
        } else {
            adViewHolder = (SmallAdViewHolder) convertView.getTag();
            viewBinder = adViewHolder.viewBinder;
        }
        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageUrl() != null) {
            Glide.with(mContext).load(ad.getImageUrl()).into(adViewHolder.mSmallImage);
        }
        return convertView;
    }


    /**
     * 非广告、内容渲染：信息流内容渲染
     */
    @SuppressWarnings("RedundantCast")
    @SuppressLint("SetTextI18n")
    private View getNormalView(View convertView, ViewGroup parent, int position, String errorMsg) {
        NormalViewHolder normalViewHolder;
        if (convertView == null || !(convertView.getTag() instanceof NormalViewHolder)) {
            normalViewHolder = new NormalViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_normal, parent, false);
            normalViewHolder.idle = (TextView) convertView.findViewById(R.id.text_idle);
            convertView.setTag(normalViewHolder);
        } else {
            normalViewHolder = (NormalViewHolder) convertView.getTag();
        }
        normalViewHolder.idle.setText("ListView item " + position + " " + errorMsg);
        return convertView;
    }


    /**
     *信息流自渲染广告绑定使用
     */
    private void bindData(View convertView, final AdViewHolder adViewHolder, final GMNativeAd ad, GMViewBinder viewBinder) {
        //设置dislike弹窗，如果有
        if (ad.hasDislike()) {
            bindNativeDislikeAction(adViewHolder.mDislike, ad);
        } else {
            if (adViewHolder.mDislike != null)
                adViewHolder.mDislike.setVisibility(View.GONE);
        }

        setDownLoadAppInfo(ad, adViewHolder);

        //设置事件回调
        ad.setNativeAdListener(new GMNativeAdListener() {
            @Override
            public void onAdClick() {
                Log.d(TAG, "onAdClick");
                TToast.show(mContext, "自渲染广告被点击");
            }

            @Override
            public void onAdShow() {
                Log.d(TAG, "onAdShow_自渲染广告");
                TToast.show(mContext, "自渲染广告展示");
                if (ad != null) {
                    GMAdEcpmInfo gmAdEcpmInfo = ad.getShowEcpm();
                    if (gmAdEcpmInfo != null) {
                        Log.e(AppConst.TAG, "adNetworkPlatformName: " + gmAdEcpmInfo.getAdNetworkPlatformName() + "   adNetworkRitId：" + gmAdEcpmInfo.getAdNetworkRitId() + "   preEcpm: " + gmAdEcpmInfo.getPreEcpm());
                    }
                }
            }
        });
        //可以被点击的view, 也可以把convertView放进来意味item可被点击
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(convertView);
        clickViewList.add(adViewHolder.mSource);
        clickViewList.add(adViewHolder.mTitle);
        clickViewList.add(adViewHolder.mDescription);
        clickViewList.add(adViewHolder.mIcon);
        //添加点击区域
        if (adViewHolder instanceof LargeAdViewHolder) {
            clickViewList.add(((LargeAdViewHolder) adViewHolder).mLargeImage);
        } else if (adViewHolder instanceof SmallAdViewHolder) {
            clickViewList.add(((SmallAdViewHolder) adViewHolder).mSmallImage);
        } else if (adViewHolder instanceof VerticalAdViewHolder) {
            clickViewList.add(((VerticalAdViewHolder) adViewHolder).mVerticalImage);
        } else if (adViewHolder instanceof VideoAdViewHolder) {
            clickViewList.add(((VideoAdViewHolder) adViewHolder).videoView);
        } else if (adViewHolder instanceof GroupAdViewHolder) {
            clickViewList.add(((GroupAdViewHolder) adViewHolder).mGroupImage1);
            clickViewList.add(((GroupAdViewHolder) adViewHolder).mGroupImage2);
            clickViewList.add(((GroupAdViewHolder) adViewHolder).mGroupImage3);
        }
        //触发创意广告的view（点击下载或拨打电话）
        List<View> creativeViewList = new ArrayList<>();
        creativeViewList.add(adViewHolder.mCreativeButton);
        //重要! 这个涉及到广告计费，必须正确调用。**** convertView必须是com.bytedance.msdk.api.format.TTNativeAdView ****
        ad.registerView((Activity) mContext, (ViewGroup) convertView, clickViewList, creativeViewList, viewBinder);

        adViewHolder.mTitle.setText(ad.getTitle()); //title为广告的简单信息提示
        adViewHolder.mDescription.setText(ad.getDescription()); //description为广告的较长的说明
        adViewHolder.mSource.setText(TextUtils.isEmpty(ad.getSource()) ? "广告来源" : ad.getSource());

        String icon = ad.getIconUrl();
        if (icon != null) {
            Glide.with(mContext).load(icon).into(adViewHolder.mIcon);
        }
        Button adCreativeButton = adViewHolder.mCreativeButton;
        switch (ad.getInteractionType()) {
            case GMAdConstant.INTERACTION_TYPE_DOWNLOAD:
                adCreativeButton.setVisibility(View.VISIBLE);
                adCreativeButton.setText(TextUtils.isEmpty(ad.getActionText()) ? "立即下载" : ad.getActionText());
                break;
            case GMAdConstant.INTERACTION_TYPE_DIAL:
                adCreativeButton.setVisibility(View.VISIBLE);
                adCreativeButton.setText("立即拨打");
                break;
            case GMAdConstant.INTERACTION_TYPE_LANDING_PAGE:
            case GMAdConstant.INTERACTION_TYPE_BROWSER:
                adCreativeButton.setVisibility(View.VISIBLE);
                adCreativeButton.setText(TextUtils.isEmpty(ad.getActionText()) ? "查看详情" : ad.getActionText());
                break;
            default:
                adCreativeButton.setVisibility(View.GONE);
                TToast.show(mContext, "交互类型异常");
        }
    }


    /**
     * 绑定自渲染信息流dislike逻辑
     *
     * @param dislike
     * @param ad
     */
    private void bindNativeDislikeAction(View dislike, final GMNativeAd ad) {
        if (dislike == null) {
            return;
        }

        final GMAdDislike ttAdDislike = ad.getDislikeDialog((Activity) mContext, null);
        dislike.setVisibility(View.VISIBLE);
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttAdDislike.setDislikeCallback(new GMDislikeCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        TToast.show(mContext, "点击 " + value);
                        //用户选择不喜欢原因后，移除广告展示
                        mData.remove(ad);
                        mCallBack.dislikeClick();//总数也要减少
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancel() {
                        TToast.show(mContext, "dislike 点击了取消");
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

                //使用接口来展示
                ttAdDislike.showDislikeDialog();
            }
        });
    }

    /**
     * 展示下载类广告的合规信息
     *
     * @param ttNativeAd
     * @param adViewHolder
     */
    private void setDownLoadAppInfo(GMNativeAd ttNativeAd, AdViewHolder adViewHolder) {
        if (adViewHolder == null) {
            return;
        }
        if (ttNativeAd == null || ttNativeAd.getNativeAdAppInfo() == null) {
            adViewHolder.app_info.setVisibility(View.GONE);
        } else {
            adViewHolder.app_info.setVisibility(View.VISIBLE);
            TTNativeAdAppInfo appInfo = ttNativeAd.getNativeAdAppInfo();
            adViewHolder.app_name.setText("应用名称：" + appInfo.getAppName());
            adViewHolder.author_name.setText("开发者：" + appInfo.getAuthorName());
            adViewHolder.package_size.setText("包大小：" + appInfo.getPackageSizeBytes());
            adViewHolder.permissions_url.setText("权限url:" + appInfo.getPermissionsUrl());
            adViewHolder.privacy_agreement.setText("隐私url：" + appInfo.getPrivacyAgreement());
            adViewHolder.version_name.setText("版本号：" + appInfo.getVersionName());
            adViewHolder.permissions_content.setText("权限内容:" + getPermissionsContent(appInfo.getPermissionsMap()));
        }
    }

    private String getPermissionsContent(Map<String, String> permissionsMap) {
        if (permissionsMap == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        Set<String> keyList = permissionsMap.keySet();
        for (String s : keyList) {
            stringBuffer.append(s + " : " + permissionsMap.get(s) + " \n");
        }

        return stringBuffer.toString();
    }

    private static class VideoAdViewHolder extends AdViewHolder {
        FrameLayout videoView;
    }

    private static class LargeAdViewHolder extends AdViewHolder {
        ImageView mLargeImage;
    }

    private static class SmallAdViewHolder extends AdViewHolder {
        ImageView mSmallImage;
    }

    private static class VerticalAdViewHolder extends AdViewHolder {
        ImageView mVerticalImage;
    }

    private static class GroupAdViewHolder extends AdViewHolder {
        ImageView mGroupImage1;
        ImageView mGroupImage2;
        ImageView mGroupImage3;
    }

    private static class ExpressAdViewHolder {
        FrameLayout mAdContainerView;
    }

    private static class AdViewHolder {
        GMViewBinder viewBinder;
        ImageView mIcon;
        ImageView mDislike;
        Button mCreativeButton;
        TextView mTitle;
        TextView mDescription;
        TextView mSource;
        RelativeLayout mLogo;

        LinearLayout app_info;
        TextView app_name;
        TextView author_name;
        TextView package_size;
        TextView permissions_url;
        TextView privacy_agreement;
        TextView version_name;
        TextView permissions_content;
    }

    private static class NormalViewHolder {
        TextView idle;
    }


    public interface DislikeCallBack {
        void dislikeClick();
    }

}
