package com.goodtech.tq.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdLoadCallback;
import com.goodtech.tq.ad.AdFeedFragment;
import com.goodtech.tq.R;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.fragment.view.CurrentItemView;
import com.goodtech.tq.fragment.view.DailyListItemView;
import com.goodtech.tq.fragment.view.HoursItemView;
import com.goodtech.tq.fragment.view.LineTempItemView;
import com.goodtech.tq.fragment.view.ObservationView;
import com.goodtech.tq.fragment.view.RecentItemView;
import com.goodtech.tq.helpers.BtnLinkHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.listener.WeatherHeaderListener;
import com.goodtech.tq.manager.AdFeedManager;
import com.goodtech.tq.models.BtnLinkModel;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.JuheAlarmModel;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.others.airQuality.AirQualityActivity;
import com.goodtech.tq.others.airQuality.view.AirLifeView;
import com.goodtech.tq.others.calendar.CalendarActivity;
import com.goodtech.tq.others.constellation.ConstellationActivity;
import com.goodtech.tq.others.taifeng.TyphoonActivity;
import com.goodtech.tq.others.test.MyTestActivity;
import com.goodtech.tq.signing.SigningActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.views.AlarmPopup;
import com.lxj.xpopup.XPopup;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment2 extends AdFeedFragment implements OnRefreshListener, WeatherHeaderListener {

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
    private FrameLayout mFeedContainer2;
    private DailyListItemView mDailyListView;
    private LineTempItemView mLineTempView;
    private AirLifeView mLifeView;
    private ObservationView mObservationView;

    private void initView(View view) {

        mContainerView = view.findViewById(R.id.weatherContainer);
        mContainerView.setVisibility(View.INVISIBLE);
        mCurrentView = view.findViewById(R.id.item_current);
        mCurrentView.setItemListener(this);
        mRecentView = view.findViewById(R.id.item_recent);
        mHoursView = view.findViewById(R.id.item_hours);
        mFeedContainer = view.findViewById(R.id.item_ad1);
        mFeedContainer2 = view.findViewById(R.id.item_ad2);
        mObservationView = view.findViewById(R.id.item_observation);
        mDailyListView = view.findViewById(R.id.item_daily_list);
        mLineTempView = view.findViewById(R.id.item_line_temp);
        mLifeView = view.findViewById(R.id.view_life);


        if (SpUtils.getInstance().isAgreePermission()) {
            initNativeExpressAD();
        }
    }

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

                if (showAd(mFeedContainer, mAdFeedManager, mLoadSuccess, mIsLoadedAndShow, mGMNativeAd)) {
                    mLoadSuccess = false;
                    mIsLoadedAndShow = true;
                }
                if (showAd(mFeedContainer2, mAdFeedManager2, mLoadSuccess2, mIsLoadedAndShow2, mGMNativeAd2)) {
                    mLoadSuccess2 = false;
                    mIsLoadedAndShow2 = true;
                }

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
    public void onTaxi() {
        String link = "https://kzurl10.cn/Z9Nks";
        String title = "免费打车券";
        BtnLinkModel model = BtnLinkHelper.getBtnLink(0);
        if (model != null) {
            link = model.getH5link();
            title = model.getTempType();
        }
        MyTestActivity.redirectTo(requireActivity(),
                link,
                title,
                "DaChe");
    }

    @Override
    public void onMeituan() {
        String link = "https://kurl04.cn/ZRSxc";
        String title = "美团大额券";
        BtnLinkModel model = BtnLinkHelper.getBtnLink(1);
        if (model != null) {
            link = model.getH5link();
            title = model.getTempType();
        }
        MyTestActivity.redirectTo(requireActivity(),
                link,
                title,
                "MeiTuan");
    }

    @Override
    public void onEleme() {
        String link = "https://kzurl05.cn/ZRJjc";
        String title = "饿了么大红包";
        BtnLinkModel model = BtnLinkHelper.getBtnLink(2);
        if (model != null) {
            link = model.getH5link();
            title = model.getTempType();
        }
        MyTestActivity.redirectTo(requireActivity(),
                link,
                title,
                "Eleme");
    }

    @Override
    public void onWarningBtn() {
        if (mWeatherModel != null && mWeatherModel.alarmModel != null) {
            AlarmPopup popup = new AlarmPopup(requireActivity());
            popup.setupData(mWeatherModel.alarmModel);
            new XPopup.Builder(requireActivity())
                    .isDestroyOnDismiss(true)
                    .asCustom(popup)
                    .show();
        }
    }

    // @Override
    // public void onDyMovie() {
    //     if (getActivity() != null) {
    //         Intent intent = new Intent(getActivity(), DyMoviesActivity.class);
    //         getActivity().startActivity(intent);
    //     }
    // }

    // <editor-fold defaultstate="collapsed" desc="广告">

    private AdFeedManager mAdFeedManager; //激励视频管理类
    private AdFeedManager mAdFeedManager2; //激励视频管理类

    private boolean mLoadSuccess; //是否加载成功
    private boolean mLoadSuccess2; //是否加载成功
    private boolean mIsLoadedAndShow;//广告加载成功并展示
    private boolean mIsLoadedAndShow2;//广告加载成功并展示

    private GMNativeAd mGMNativeAd; //原生广告model
    private GMNativeAd mGMNativeAd2; //原生广告model

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
                    boolean isShow = showAd(mFeedContainer, mAdFeedManager, true, false, mGMNativeAd);
                    if (isShow) {
                        mLoadSuccess = false;
                        mIsLoadedAndShow = true;
                    }
                }
            }

            @Override
            public void onAdLoadedFail(AdError adError) {
                //TToast.show(getContext(), "广告加载失败！");
                Log.e(TAG, "load feed ad error : " + adError.code + ", " + adError.message);
            }
        });
        mAdFeedManager2 = new AdFeedManager(requireActivity(), new GMNativeAdLoadCallback() {
            @Override
            public void onAdLoaded(List<GMNativeAd> ads) {
                if (ads == null || ads.isEmpty()) {
                    Log.e(TAG, "on FeedAdLoaded: ad is null!");
                    //TToast.show(getContext(), "广告加载失败！");
                    return;
                }
                mLoadSuccess2 = true;

                mGMNativeAd2 = ads.get(0);
                if (mGMNativeAd2 != null && !mIsLoadedAndShow2) {
                    boolean isShow = showAd(mFeedContainer2, mAdFeedManager2, true, false, mGMNativeAd2);
                    if (isShow) {
                        mLoadSuccess2 = false;
                        mIsLoadedAndShow2 = true;
                    }
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
        mLoadSuccess2 = false;
        mFeedContainer.removeAllViews();
        mFeedContainer2.removeAllViews();
        Log.e(TAG, "initNativeExpressAD: ++++ " + System.currentTimeMillis());
        mAdFeedManager.loadAdWithCallback(Constants.PGE_EXPRESS_POS_ID3, 1, GMAdConstant.IMAGE_MODE_SMALL_IMG, ((int) DeviceUtils.getScreenWidthDpi(requireActivity()) - 28));
        mAdFeedManager2.loadAdWithCallback(Constants.PGE_EXPRESS_POS_ID, 1, GMAdConstant.IMAGE_MODE_SMALL_IMG, ((int) DeviceUtils.getScreenWidthDpi(requireActivity()) - 28));
    }

    @Override
    protected void removeAdView(GMNativeAd ad) {
        if (mFeedContainer != null && mGMNativeAd == ad) {
            mFeedContainer.removeAllViews();
            mFeedContainer.setVisibility(View.GONE);
        }
        if (mFeedContainer2 != null && mGMNativeAd2 == ad) {
            mFeedContainer2.removeAllViews();
            mFeedContainer2.setVisibility(View.GONE);
        }
    }

    // </editor-fold>
}
