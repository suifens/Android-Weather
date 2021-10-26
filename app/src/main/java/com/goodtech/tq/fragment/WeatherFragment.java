package com.goodtech.tq.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.adapter.WeatherRecyclerAdapter;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.listener.WeatherHeaderListener;
import com.goodtech.tq.models.CityMode;
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
public class WeatherFragment extends BaseFragment implements OnRefreshListener, NativeExpressAD.NativeExpressADListener {

    private static final String TAG = "WeatherFragment";
    protected SmartRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected WeatherRecyclerAdapter mAdapter;
    protected WeatherModel mModel;

    protected View mStateBarBg;

    protected CityMode mCityMode;

    @Override
    protected int getViewLayoutRes() {
        return R.layout.fragment_item_list;
    }

    @Override
    protected void setupCacheViews() {
        super.setupCacheViews();
        mRefreshLayout = (SmartRefreshLayout) mCacheView;
        mRecyclerView = mCacheView.findViewById(R.id.list);
    }

    public void setStateBar(View stateBar) {
        this.mStateBarBg = stateBar;
    }

    private int totalDy = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRefreshLayout.setOnRefreshListener(this);
//        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
//            refreshLayout.finishLoadMore();
//            Intent intent = new Intent(getActivity(), NewsActivity.class);
//            requireActivity().startActivity(intent);
//        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new WeatherRecyclerAdapter(getContext(), mModel, mCityMode != null ? mCityMode.getCity() : null);
        mAdapter.setHeaderListener(mHeaderListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalDy += dy;
                if (totalDy <= mStateBarBg.getHeight() && totalDy > 10) {
                    float alpha = (totalDy) / (float) (mStateBarBg.getHeight() * 1.0);
                    mStateBarBg.setAlpha(alpha);
                } else if (totalDy > mStateBarBg.getHeight()) {
                    mStateBarBg.setAlpha(1);
                } else {
                    mStateBarBg.setAlpha(0);
                }
            }
        });

        initNativeExpressAD();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
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

        @Override
        public void onNews() {

        }

        @Override
        public void onSignIn() {

        }
    };

    @SuppressLint("NotifyDataSetChanged")
    public void changeWeather(WeatherModel model, CityMode cityMode) {

        if (mModel != null
                && model != null
                && model.expireTime == mModel.expireTime) {
            return;
        }

        this.mModel = model;
        this.mCityMode = cityMode;
        if (mAdapter != null && cityMode != null) {
            mAdapter.notifyDataSetChanged(model, cityMode.getCity());
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

    /**
     * AD
     */

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
            mAdapter.changeAdView(mAdView);
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
        if (mAdapter != null) {
            mAdapter.changeAdView(adView);
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
