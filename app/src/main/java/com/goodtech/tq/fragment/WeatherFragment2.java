package com.goodtech.tq.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import com.bytedance.msdk.api.nativeAd.TTNativeAdAppInfo;
import com.bytedance.msdk.api.nativeAd.TTViewBinder;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMAdDislike;
import com.bytedance.msdk.api.v2.GMAdSize;
import com.bytedance.msdk.api.v2.GMDislikeCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdLoadCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeExpressAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMVideoListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMViewBinder;
import com.goodtech.tq.R;
import com.goodtech.tq.fragment.view.CurrentItemView;
import com.goodtech.tq.fragment.view.DailyItemView;
import com.goodtech.tq.fragment.view.HoursItemView;
import com.goodtech.tq.fragment.view.LineTempItemView;
import com.goodtech.tq.fragment.view.ObservationView;
import com.goodtech.tq.fragment.view.RecentItemView;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.listener.WeatherHeaderListener;
import com.goodtech.tq.manager.AdFeedManager;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.news.NewsActivity;
import com.goodtech.tq.others.airQuality.AirQualityActivity;
import com.goodtech.tq.others.calendar.CalendarActivity;
import com.goodtech.tq.others.constellation.ConstellationActivity;
import com.goodtech.tq.others.dymovies.DyMoviesActivity;
import com.goodtech.tq.others.outbreak.OutbreakActivity;
import com.goodtech.tq.others.taifeng.TyphoonActivity;
import com.goodtech.tq.signing.SigningActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
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
    protected ViewPager2 mDailyPager;

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
            if (scrollY <= mStateBarBg.getHeight() && scrollY > 10) {
                float alpha = (totalDy) / (float) (mStateBarBg.getHeight() * 1.0);
                mStateBarBg.setAlpha(alpha);
            } else if (scrollY > mStateBarBg.getHeight()) {
                mStateBarBg.setAlpha(1);
            } else {
                mStateBarBg.setAlpha(0);
            }
        });

        initAdLoader();
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
    private TextView mListBtnTv;
    private TextView mLineBtnTv;
    
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

        mDailyPager = view.findViewById(R.id.dailyViewPager);

        mListBtnTv = view.findViewById(R.id.tv_daily);
        mListBtnTv.setOnClickListener(v -> {
            onSegmentClick(0);
        });
        mLineBtnTv = view.findViewById(R.id.tv_line);
        mLineBtnTv.setOnClickListener(v -> {
            onSegmentClick(1);
        });

        configViewPager2();

        onSegmentClick(0);
        initNativeExpressAD();
    }

    private void onSegmentClick(int index) {
        if (index == 0) {
            mDailyPager.setCurrentItem(0, true);
            mListBtnTv.setBackgroundResource(R.drawable.bg_circle_5a9ef2_6);
            mLineBtnTv.setBackgroundResource(R.color.color_clear);
        } else {
            mDailyPager.setCurrentItem(1, true);
            mListBtnTv.setBackgroundResource(R.color.color_clear);
            mLineBtnTv.setBackgroundResource(R.drawable.bg_circle_5a9ef2_6);
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

                if (mListFragment != null) {
                    mListFragment.setData(mWeatherModel);
                }

                if (mLineFragment != null) {
                    mLineFragment.setData(mWeatherModel);
                }
            });
        }
    }


    private DailyListFragment mListFragment;
    private DailyLineFragment mLineFragment;
    private void configViewPager2() {
        this.mDailyPager.setUserInputEnabled(false);
        this.mDailyPager.setOffscreenPageLimit(2);
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return 2;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    mListFragment = new DailyListFragment();
                    return mListFragment;
                } else {
                    mLineFragment = new DailyLineFragment();
                    return mLineFragment;
                }
            }
        };
        mDailyPager.setAdapter(adapter);
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
        mAdFeedManager.loadAdWithCallback(Constants.PGE_EXPRESS_POS_ID, 1, GMAdConstant.TYPE_EXPRESS_AD, ((int) DeviceUtils.getScreenWidthDpi(requireActivity().getApplicationContext()) - 28));
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
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            //TToast.show(requireActivity(), "图片展示样式错误");
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

    private void removeAdView() {
        if (mFeedContainer != null) {
            mFeedContainer.removeAllViews();
        }
    }
}
