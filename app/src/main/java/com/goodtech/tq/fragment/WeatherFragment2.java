package com.goodtech.tq.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.Glide;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.TToast;
import com.bytedance.msdk.api.UIUtils;
import com.bytedance.msdk.api.nativeAd.TTNativeAdAppInfo;
import com.bytedance.msdk.api.nativeAd.TTViewBinder;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMAdDislike;
import com.bytedance.msdk.api.v2.GMAdSize;
import com.bytedance.msdk.api.v2.GMDislikeCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdLoadCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeCustomVideoReporter;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeExpressAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMVideoListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMViewBinder;
import com.goodtech.tq.R;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.activity.MainActivity;
import com.goodtech.tq.activity.SettingActivity;
import com.goodtech.tq.fragment.view.CurrentItemView;
import com.goodtech.tq.fragment.view.DailyItemView;
import com.goodtech.tq.fragment.view.DailyListItemView;
import com.goodtech.tq.fragment.view.HoursItemView;
import com.goodtech.tq.fragment.view.LineTempItemView;
import com.goodtech.tq.fragment.view.ObservationView;
import com.goodtech.tq.fragment.view.RecentItemView;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.listener.WeatherHeaderListener;
import com.goodtech.tq.manager.AdFeedManager;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.others.airQuality.AirQualityActivity;
import com.goodtech.tq.others.airQuality.view.AirLifeView;
import com.goodtech.tq.others.calendar.CalendarActivity;
import com.goodtech.tq.others.constellation.ConstellationActivity;
import com.goodtech.tq.others.dymovies.DyMoviesActivity;
import com.goodtech.tq.others.outbreak.OutbreakActivity;
import com.goodtech.tq.others.taifeng.TyphoonActivity;
import com.goodtech.tq.signing.SigningActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.SpUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment2 extends BaseFragment implements OnRefreshListener {

    private static final String TAG = "WeatherFragment2";
    protected SmartRefreshLayout mRefreshLayout;
    protected NestedScrollView mScrollView;
    protected WeatherModel mWeatherModel;

    protected View mStateBarBg;

    protected CityMode mCityMode;

    protected boolean mHadLoad;

    @Override
    protected int getViewLayoutRes() {
        return R.layout.fragment_weather;
    }

    @Override
    protected void setupCacheViews() {
        super.setupCacheViews();
        mRefreshLayout = (SmartRefreshLayout) mCacheView;
        mScrollView = mCacheView.findViewById(R.id.scroll_view);
    }

    public void setStateBar(View stateBar) {
        this.mStateBarBg = stateBar;
    }

    private final int totalDy = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRefreshLayout.setOnRefreshListener(this);

        mScrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (mStateBarBg != null) {
                if (scrollY <= mStateBarBg.getHeight() && scrollY > 10) {
                    float alpha = (totalDy) / (float) (mStateBarBg.getHeight() * 1.0);
                    mStateBarBg.setAlpha(alpha);
                } else if (scrollY > mStateBarBg.getHeight()) {
                    mStateBarBg.setAlpha(1);
                } else {
                    mStateBarBg.setAlpha(0);
                }
            }
        });

        if (SpUtils.getInstance().isAgreePermission()) {
            initAdLoader();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewStub viewStub = mCacheView.findViewById(R.id.stub_weather_data);
        if (viewStub != null && viewStub.getParent() != null) {         // 判断是否第一次加载
            View inflate = viewStub.inflate();      // 布局加载
            initView(inflate);
            mHadLoad = true;
            updateData();
        }

        if (mGMNativeAd != null) {
            mGMNativeAd.resume();
        }
    }

    private View mContainerView;
    private CurrentItemView mCurrentView;
    private RecentItemView mRecentView;
    private HoursItemView mHoursView;
    private FrameLayout mFeedContainer;
    private DailyListItemView mDailyListView;
    private LineTempItemView mLineTempView;
    private AirLifeView mLifeView;
    private ObservationView mObservationView;

    private void initView(View view) {

        mContainerView = view.findViewById(R.id.weatherContainer);
        mContainerView.setVisibility(View.INVISIBLE);
        mCurrentView = view.findViewById(R.id.item_current);
        mCurrentView.setItemListener(mHeaderListener);
        mRecentView = view.findViewById(R.id.item_recent);
        mHoursView = view.findViewById(R.id.item_hours);
        mFeedContainer = view.findViewById(R.id.item_ad);
        mObservationView = view.findViewById(R.id.item_observation);
        mDailyListView = view.findViewById(R.id.item_daily_list);
        mLineTempView = view.findViewById(R.id.item_line_temp);
        mLifeView = view.findViewById(R.id.view_life);

        if (SpUtils.getInstance().isAgreePermission()) {
            initNativeExpressAD();
        }
    }

    private final WeatherHeaderListener mHeaderListener = new WeatherHeaderListener() {
        @Override
        public void onTyphoon() {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), TyphoonActivity.class);
                getActivity().startActivity(intent);
            }
        }

        @Override
        public void onAirQuality() {
            if (getActivity() != null) {
                AirQualityActivity.redirectTo(getActivity(), mCityMode, mWeatherModel.aqi);
            }
        }

        @Override
        public void onCalendar() {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), CalendarActivity.class);
                getActivity().startActivity(intent);
            }
        }

        @Override
        public void onFortune() {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ConstellationActivity.class);
                getActivity().startActivity(intent);
            }
        }

        // @Override
        // public void onNews() {
        //     if (getActivity() != null) {
        //         Intent intent = new Intent(getActivity(), NewsActivity.class);
        //         requireActivity().startActivity(intent);
        //     }
        // }

        @Override
        public void onSignIn() {
            if (getActivity() != null) {
                if (!SpUtils.getInstance().isAgreePermission()) {
                    ((BaseActivity) requireActivity()).showPermissionDialog(requireActivity(), view ->
                            SigningActivity.redirectTo(getActivity(), mWeatherModel.hourlies.get(0), mCityMode, 0));
                    return;
                }
                SigningActivity.redirectTo(getActivity(), mWeatherModel.hourlies.get(0), mCityMode, 0);
            }
        }

        @Override
        public void onOutbreakTravel() {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), OutbreakActivity.class);
                getActivity().startActivity(intent);
            }
        }

        @Override
        public void onDyMovie() {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), DyMoviesActivity.class);
                getActivity().startActivity(intent);
            }
        }
    };

    @Override
    public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
        boolean fetching = WeatherHttpHelper.getInstance().fetchWeather(mCityMode,
                (success, weather, errCode) ->
                        mHandler.post(() -> {
                            if (weather != null && mCityMode != null) {
                                changeWeather(weather, mCityMode);
                            }
                            refreshLayout.finishRefresh();
                        }));

        if (!fetching) {
            //  无需刷新，则直接消失刷新
            mHandler.postDelayed(refreshLayout::finishRefresh, 300);
        }
    }

    public void changeWeather(WeatherModel model, CityMode cityMode) {
        this.mWeatherModel = model;
        this.mCityMode = cityMode;
        updateData();
    }

    private void updateData() {
        if (mHadLoad && mCurrentView != null && mWeatherModel != null) {
            mHandler.post(() -> {

                showAd();

                mContainerView.setVisibility(View.VISIBLE);

                mCurrentView.setData(mWeatherModel);
                mRecentView.setData(mWeatherModel);
                if (mWeatherModel.hourlies != null) {
                    mHoursView.setHourlies(mWeatherModel);
                }
                
                if (mCityMode != null) {
                    mObservationView.setData(mWeatherModel);
                }

                if (mWeatherModel.dailies != null) {
                    mDailyListView.setData(mWeatherModel);
                    mLineTempView.setData(mWeatherModel);
                }

                if (mWeatherModel.lifeModel != null) {
                    mLifeView.setupLife(mWeatherModel.lifeModel, Color.WHITE);
                }
            });
        }
    }

    /**
     * AD
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdFeedManager != null) {
            mAdFeedManager.destroy();
        }
        mGMNativeAd = null;
    }

    // <editor-fold defaultstate="collapsed" desc="广告">

    private AdFeedManager mAdFeedManager; //激励视频管理类

    private boolean mLoadSuccess; //是否加载成功
    private boolean mIsLoadedAndShow;//广告加载成功并展示

    private GMNativeAd mGMNativeAd; //原生广告model

    private void initAdLoader() {
        mAdFeedManager = new AdFeedManager(requireActivity(), new GMNativeAdLoadCallback() {
            @Override
            public void onAdLoaded(List<GMNativeAd> ads) {
                if (ads == null || ads.isEmpty()) {
                    Log.e(TAG, "on FeedAdLoaded: ad is null!");
                    //TToast.show(getContext(), "广告加载失败！");
                    return;
                }
                mLoadSuccess = true;

                mGMNativeAd = ads.get(0);
                if (mGMNativeAd != null && !mIsLoadedAndShow) {
                    showAd();
                }
            }

            @Override
            public void onAdLoadedFail(AdError adError) {
                //TToast.show(getContext(), "广告加载失败！");
                Log.e(TAG, "load feed ad error : " + adError.code + ", " + adError.message);
            }
        });
    }

    private void initNativeExpressAD() {
        mLoadSuccess = false;
        removeAdView();
        mAdFeedManager.loadAdWithCallback(Constants.PGE_EXPRESS_POS_ID, 1, GMAdConstant.IMAGE_MODE_SMALL_IMG, ((int) DeviceUtils.getScreenWidthDpi(requireActivity()) - 28));
    }

    /**
     * 展示原生广告
     */
    private void showAd() {
        if (!mLoadSuccess || mAdFeedManager == null || mGMNativeAd == null) {
            //TToast.show(getContext(), "请先加载广告");
            // initNativeExpressAD();
            return;
        }
        if (!mGMNativeAd.isReady()) {
            //TToast.show(getContext(), "广告已经无效，请重新请求");
            // initNativeExpressAD();
            return;
        }
        mLoadSuccess = false;
        mIsLoadedAndShow = true;

        mFeedContainer.setVisibility(View.VISIBLE);
        View view = null;
        if (mGMNativeAd.isExpressAd()) { //模板
            view = getExpressAdView(mFeedContainer, mGMNativeAd);
        } else if (mGMNativeAd.getAdImageMode() == GMAdConstant.IMAGE_MODE_SMALL_IMG) { //原生小图
            view = getSmallAdView(mFeedContainer, mGMNativeAd);

        } else if (mGMNativeAd.getAdImageMode() == GMAdConstant.IMAGE_MODE_LARGE_IMG) {//原生大图
            view = getLargeAdView(mFeedContainer, mGMNativeAd);

        } else if (mGMNativeAd.getAdImageMode() == GMAdConstant.IMAGE_MODE_GROUP_IMG) {//原生组图
            view = getGroupAdView(mFeedContainer, mGMNativeAd);

        } else if (mGMNativeAd.getAdImageMode() == GMAdConstant.IMAGE_MODE_VIDEO) {//原生视频
            view = getVideoView(mFeedContainer, mGMNativeAd);

        } else if (mGMNativeAd.getAdImageMode() == GMAdConstant.IMAGE_MODE_VERTICAL_IMG) {//原生竖版图片
            view = getVerticalAdView(mFeedContainer, mGMNativeAd);

        } else if (mGMNativeAd.getAdImageMode() == GMAdConstant.IMAGE_MODE_VIDEO_VERTICAL) {//原生视频
            view = getVideoView(mFeedContainer, mGMNativeAd);
        } else {
            // TToast.show(getContext(), "图片展示样式错误");
        }

        if (view != null) {
            view.setLayoutParams(new
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mFeedContainer.removeAllViews();
            mFeedContainer.addView(view);
        }
    }

    //渲染模板广告
    @SuppressWarnings("RedundantCast")
    private View getExpressAdView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        final ExpressAdViewHolder adViewHolder;
        View convertView = null;
        try {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_ad_native_express, parent, false);
            adViewHolder = new ExpressAdViewHolder();
            adViewHolder.mAdContainerView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_express);
            convertView.setTag(adViewHolder);

            //判断是否存在dislike按钮
            if (ad.hasDislike()) {
                ad.setDislikeCallback((Activity) requireActivity(), new GMDislikeCallback() {
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
                            sWidth = DeviceUtils.getScreenWidth(requireActivity()) - SizeUtils.dp2px(28);
                            sHeight = (int) ((sWidth * height) / width);
                        }
                        if (video != null) {
                            /**
                             * 如果存在父布局，需要先从父布局中移除
                             */
                            DeviceUtils.removeFromParent(video);
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sWidth, sHeight);
                            adViewHolder.mAdContainerView.removeAllViews();
                            video.setBackgroundColor(Color.TRANSPARENT);
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

    private static class ExpressAdViewHolder {
        FrameLayout mAdContainerView;
    }

    /**
     * @param parent
     * @param ad
     * @return
     */
    private View getVerticalAdView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        VerticalAdViewHolder adViewHolder;
        View convertView = null;
        GMViewBinder viewBinder;
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_ad_vertical_pic, parent, false);
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
        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageUrl() != null) {
            Glide.with(getContext()).load(ad.getImageUrl()).into(adViewHolder.mVerticalImage);
        }

        return convertView;
    }

    //渲染视频广告，以视频广告为例，以下说明
    @SuppressWarnings("RedundantCast")
    private View getVideoView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        VideoAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        View convertView = null;
        try {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_ad_large_video, parent, false);
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

            // 如果自己有需求进行视频播放操作，则按如下步骤进行。注意：不是所有adn都支持，因此需要进行判空操作。
            ad.setUseCustomVideo(true); // 注意：要在registerView之前调用
            String videoUrl = ad.getVideoUrl(); // 注意：不是所有adn都支持，这里务必要进行判空操作
            if (!TextUtils.isEmpty(videoUrl)) {
                // 如果通过getVideoUrl返回的url不是空，则自己处理视频播放操作，并通过reporter上报视频状态
                playAdVideo(ad, adViewHolder.videoView, videoUrl);
            } else {
                // 注意：即使该adn支持返回url，但某次偶然原因返回的videoUrl为空，则仍然会用adn自己的视频播放作为兜底
            }

            //视频广告设置播放状态回调（可选）
            ad.setVideoListener(new GMVideoListener() {

                @Override
                public void onVideoStart() {
                    TToast.show(getContext(), "广告视频开始播放");
                    Log.d(TAG, "onVideoStart");
                }

                @Override
                public void onVideoPause() {
                    TToast.show(getContext(), "广告视频暂停");
                    Log.d(TAG, "onVideoPause");
                }

                @Override
                public void onVideoResume() {
                    TToast.show(getContext(), "广告视频继续播放");
                    Log.d(TAG, "onVideoResume");
                }

                @Override
                public void onVideoCompleted() {
                    TToast.show(getContext(), "广告播放完成");
                    Log.d(TAG, "onVideoCompleted");
                }

                @Override
                public void onVideoError(AdError adError) {
                    TToast.show(getContext(), "广告视频播放出错");
                    Log.d(TAG, "onVideoError");
                }

                @Override
                public void onProgressUpdate(long l, long l1) {

                }
            });

            //绑定广告数据、设置交互回调
            bindData(convertView, adViewHolder, ad, viewBinder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private VideoView mVideoView;
    private GMNativeCustomVideoReporter customVideoReporter;
    private void playAdVideo(GMNativeAd ad, ViewGroup videoContainer, String videoUrl) {
        mVideoView = new VideoView(this.getContext());
        videoContainer.addView(mVideoView);
        customVideoReporter = ad.getGMNativeCustomVideoReporter();
        // 开发者根据自己的需求，做相应的视频控制界面，播放暂停等

        mVideoView.setVideoURI(Uri.parse(videoUrl));
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (customVideoReporter != null) {
                    customVideoReporter.reportVideoError(mVideoView.getCurrentPosition(), what, extra);
                }
                return false;
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (customVideoReporter != null) {
                    customVideoReporter.reportVideoFinish();
                }
            }
        });
        mVideoView.start();
        if (customVideoReporter != null) {
            customVideoReporter.reportVideoStart();
        }
    }

    @SuppressWarnings("RedundantCast")
    private View getLargeAdView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        final LargeAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        View convertView = null;
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_ad_large_pic, parent, false);
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
        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageUrl() != null) {
            Glide.with(getContext()).load(ad.getImageUrl()).into(adViewHolder.mLargeImage);
        }
        return convertView;
    }

    @SuppressWarnings("RedundantCast")
    private View getGroupAdView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        GroupAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        View convertView = null;
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_ad_group_pic, parent, false);
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

        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageList() != null && ad.getImageList().size() >= 3) {
            String image1 = ad.getImageList().get(0);
            String image2 = ad.getImageList().get(1);
            String image3 = ad.getImageList().get(2);
            if (image1 != null) {
                Glide.with(getContext()).load(image1).into(adViewHolder.mGroupImage1);
            }
            if (image2 != null) {
                Glide.with(getContext()).load(image2).into(adViewHolder.mGroupImage2);
            }
            if (image3 != null) {
                Glide.with(getContext()).load(image3).into(adViewHolder.mGroupImage3);
            }
        }
        return convertView;
    }


    @SuppressWarnings("RedundantCast")
    private View getSmallAdView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        SmallAdViewHolder adViewHolder;
        GMViewBinder viewBinder;
        View convertView = null;
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_ad_small_pic, parent, false);
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
        bindData(convertView, adViewHolder, ad, viewBinder);
        if (ad.getImageUrl() != null) {
            Glide.with(getContext()).load(ad.getImageUrl()).into(adViewHolder.mSmallImage);
        }
        return convertView;
    }


    GMNativeAdListener mTTNativeAdListener = new GMNativeAdListener() {
        @Override
        public void onAdClick() {
            Log.d(TAG, "onAdClick");
            TToast.show(getContext(), "自渲染广告被点击");
        }


        @Override
        public void onAdShow() {
            Log.d(TAG, "onAdShow");
            TToast.show(getContext(), "广告展示");
        }
    };

    private void bindData(View convertView, final AdViewHolder adViewHolder, final GMNativeAd ad, GMViewBinder viewBinder) {
        //设置dislike弹窗，如果有
        if (ad.hasDislike()) {
            final GMAdDislike ttAdDislike = ad.getDislikeDialog((Activity) getContext());
            adViewHolder.mDislike.setVisibility(View.VISIBLE);
            adViewHolder.mDislike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //使用接口来展示
                    ttAdDislike.showDislikeDialog();
                    ttAdDislike.setDislikeCallback(new GMDislikeCallback() {
                        @Override
                        public void onSelected(int position, String value) {
                            TToast.show(getContext(), "点击 " + value);
                            //用户选择不喜欢原因后，移除广告展示
                            removeAdView();
                        }

                        @Override
                        public void onCancel() {
                            TToast.show(getContext(), "dislike 点击了取消");
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
            });
        } else {
            if (adViewHolder.mDislike != null)
                adViewHolder.mDislike.setVisibility(View.GONE);
        }

        setDownLoadAppInfo(ad, adViewHolder);

        //设置事件回调
        ad.setNativeAdListener(mTTNativeAdListener);
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
        ad.registerView(requireActivity(),(ViewGroup) convertView, clickViewList, creativeViewList, viewBinder);

        adViewHolder.mTitle.setText(ad.getTitle()); //title为广告的简单信息提示
        adViewHolder.mDescription.setText(ad.getDescription()); //description为广告的较长的说明
        adViewHolder.mSource.setText(TextUtils.isEmpty(ad.getSource()) ? "广告来源" : ad.getSource());

        String icon = ad.getIconUrl();
        if (icon != null) {
            Glide.with(getContext()).load(icon).into(adViewHolder.mIcon);
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
                TToast.show(getContext(), "交互类型异常");
        }
    }


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

    private void removeAdView() {
        if (mFeedContainer != null) {
            mFeedContainer.removeAllViews();
        }
    }
    // </editor-fold>
}
