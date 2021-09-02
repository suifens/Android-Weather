package com.goodtech.tq.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.view.CurrentItemView;
import com.goodtech.tq.fragment.view.DailyItemView;
import com.goodtech.tq.fragment.view.HoursItemView;
import com.goodtech.tq.fragment.view.LineTempItemView;
import com.goodtech.tq.fragment.view.NativeExpressAD2View;
import com.goodtech.tq.fragment.view.ObservationItemView;
import com.goodtech.tq.fragment.view.RecentItemView;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.listener.WeatherHeaderListener;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.others.airQuality.AirQualityActivity;
import com.goodtech.tq.others.calendar.CalendarActivity;
import com.goodtech.tq.others.constellation.ConstellationActivity;
import com.goodtech.tq.others.taifeng.TyphoonActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DownloadConfirmHelper;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment2 extends BaseFragment implements OnRefreshListener, NativeExpressAD.NativeExpressADListener {

    private static final String TAG = "WeatherFragment2";
    protected SmartRefreshLayout mRefreshLayout;
    protected NestedScrollView mScrollView;
    protected WeatherModel mModel;

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

    private int totalDy = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        initNativeExpressAD();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewStub viewStub = mCacheView.findViewById(R.id.stub_weather_data);
        if (viewStub != null && viewStub.getParent() != null) {         // 判断是否第一次加载
            View inflate = viewStub.inflate();      // 布局加载
            initView(inflate);
            mHadLoad = true;
            updateData();
        }
    }

    private CurrentItemView mCurrentView;
    private RecentItemView mRecentView;
    private HoursItemView mHoursView;
    private DailyItemView mDailyView1;
    private DailyItemView mDailyView2;
    private DailyItemView mDailyView3;
    private DailyItemView mDailyView4;
    private DailyItemView mDailyView5;
    private DailyItemView mDailyView6;
    private DailyItemView mDailyView7;
    private NativeExpressAD2View mAd2View;
    private LineTempItemView mLineTempView;
    private ObservationItemView mObservationView;

    private void initView(View view) {
        mCurrentView = view.findViewById(R.id.item_current);
        mCurrentView.setVisibility(View.GONE);
        mCurrentView.setItemListener(mHeaderListener);
        
        mRecentView = view.findViewById(R.id.item_recent);
        mRecentView.setVisibility(View.GONE);
        
        mHoursView = view.findViewById(R.id.item_hours);
        mHoursView.setVisibility(View.GONE);

        mDailyView1 = view.findViewById(R.id.item_daily_1);
        mDailyView1.setVisibility(View.GONE);
        
        mDailyView2 = view.findViewById(R.id.item_daily_2);
        mDailyView2.setVisibility(View.GONE);
        
        mDailyView3 = view.findViewById(R.id.item_daily_3);
        mDailyView3.setVisibility(View.GONE);
        
        mDailyView4 = view.findViewById(R.id.item_daily_4);
        mDailyView4.setVisibility(View.GONE);
        
        mDailyView5 = view.findViewById(R.id.item_daily_5);
        mDailyView5.setVisibility(View.GONE);
        
        mDailyView6 = view.findViewById(R.id.item_daily_6);
        mDailyView6.setVisibility(View.GONE);
        
        mDailyView7 = view.findViewById(R.id.item_daily_7);
        mDailyView7.setVisibility(View.GONE);
        
        mAd2View = view.findViewById(R.id.item_ad);
        mAd2View.setVisibility(View.GONE);
        
        mLineTempView = view.findViewById(R.id.item_line_temp);
        mLineTempView.setVisibility(View.GONE);
        
        mObservationView = view.findViewById(R.id.item_observation);
        mObservationView.setVisibility(View.GONE);
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
                Intent intent = new Intent(getActivity(), AirQualityActivity.class);
                getActivity().startActivity(intent);
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

        if (mModel != null
                && model != null) {
//                && model.expireTime == mModel.expireTime) {
            return;
        }

        this.mModel = model;
        this.mCityMode = cityMode;
        updateData();
    }

    private void updateData() {
        if (mHadLoad && mCurrentView != null && mModel != null) {
            mHandler.post(() -> {
                Log.e(TAG, "updateData: " + mModel.toString());
                mCurrentView.setData(mModel);
                mRecentView.setData(mModel);
                if (mModel.hourlies != null) {
                    mHoursView.setHourlies(mModel);
                }
                if (mModel.dailies != null) {
                    mLineTempView.setData(mModel);
                }

                for (int i = 0; i < 7; i++) {
                    if (mModel.dailies != null && mModel.dailies.size() > i) {
                        Daily daily = mModel.dailies.get(i);
                        switch (i) {
                            case 0:
                                mDailyView1.setData(mModel, daily);
                                break;
                            case 1:
                                mDailyView2.setData(mModel, daily);
                                break;
                            case 2:
                                mDailyView3.setData(mModel, daily);
                                break;
                            case 3:
                                mDailyView4.setData(mModel, daily);
                                break;
                            case 4:
                                mDailyView5.setData(mModel, daily);
                                break;
                            case 5:
                                mDailyView6.setData(mModel, daily);
                                break;
                            case 6:
                                mDailyView7.setData(mModel, daily);
                                break;
                        }
                    }
                }

                if (mCityMode != null) {
                    mObservationView.setData(mModel, mCityMode.city);
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
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    private int mReloadCount = 1;
    private NativeExpressAD mADManager;
    private NativeExpressADView mAdView;

    private void initNativeExpressAD() {
        ADSize adSize = new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT); // 消息流中用AUTO_HEIGHT
        mADManager = new NativeExpressAD(getContext(), adSize, Constants.EXPRESS_POS_ID, this);
        mADManager.loadAD(1);

    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        Log.e(TAG, "onADLoaded: " + adList.size());
        if (adList.size() > 0) {
            mAdView = adList.get(0);
            if (DownloadConfirmHelper.USE_CUSTOM_DIALOG) {
                mAdView.setDownloadConfirmListener(DownloadConfirmHelper.DOWNLOAD_CONFIRM_LISTENER);
            }
            mAd2View.setAdView(mAdView);
            mAd2View.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail: " + adView.toString());
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess: " + adView.toString());
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        Log.i(TAG, "onADExposure: " + adView.toString());
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        Log.i(TAG, "onADClicked: " + adView.toString());
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        Log.i(TAG, "onADClosed: " + adView.toString());
        if (mAd2View != null) {
            mAd2View.setAdView(adView);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        Log.i(TAG, "onADLeftApplication: " + adView.toString());
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADOpenOverlay: " + adView.toString());
    }

    @Override
    public void onADCloseOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADCloseOverlay");
    }

    @Override
    public void onNoAD(AdError error) {
        Log.i(TAG, String.format("onNoAD: error code : %d, error msg %s", error.getErrorCode(),
                error.getErrorMsg()));
        if (mReloadCount <= 3) {
            mReloadCount++;
            mADManager.loadAD(1);
        }
    }
}
