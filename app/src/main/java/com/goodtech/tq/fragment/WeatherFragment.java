package com.goodtech.tq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.adapter.WeatherRecyclerAdapter;
import com.goodtech.tq.httpClient.ApiCallback;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.news.NewsActivity;
import com.goodtech.tq.utils.Constants;
import com.qq.e.ads.nativ.express2.AdEventListener;
import com.qq.e.ads.nativ.express2.MediaEventListener;
import com.qq.e.ads.nativ.express2.NativeExpressAD2;
import com.qq.e.ads.nativ.express2.NativeExpressADData2;
import com.qq.e.comm.util.AdError;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;
import java.util.Objects;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment extends BaseFragment implements OnRefreshListener, NativeExpressAD2.AdLoadListener {

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
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore();
                Intent intent = new Intent(getActivity(), NewsActivity.class);
                Objects.requireNonNull(getActivity()).startActivity(intent);
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new WeatherRecyclerAdapter(getContext(), mModel, mCityMode != null ? mCityMode.city : null);
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
                    float alpha = (totalDy)/(float)(mStateBarBg.getHeight() * 1.0);
                    mStateBarBg.setAlpha(alpha);
                }
                else if (totalDy > mStateBarBg.getHeight()) {
                    mStateBarBg.setAlpha(1);
                }
                else {
                    mStateBarBg.setAlpha(0);
                }
            }
        });

        initNativeExpressAD2();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNativeExpressADData2 != null) {
            mNativeExpressADData2.destroy();
        }
    }

    public void changeWeather(WeatherModel model, CityMode cityMode) {

        if (mModel != null
                && cityMode != null
                && model.expireTime == mModel.expireTime) {
            return;
        }

        this.mModel = model;
        this.mCityMode = cityMode;
        if (mAdapter != null && cityMode != null) {
            mAdapter.notifyDataSetChanged(model, cityMode.city);
        }
    }

    @Override
    public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
        boolean fetching = WeatherHttpHelper.getInstance().fetchWeather(mCityMode, new ApiCallback() {
            @Override
            public void onResponse(boolean success, final WeatherModel weather, ErrorCode errCode) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null) {
                            changeWeather(weather, mCityMode);
                        }
                        refreshLayout.finishRefresh();
                    }
                });
            }
        });

        if (!fetching) {
            //  无需刷新，则直接消失刷新
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.finishRefresh();
                }
            }, 300);
        }
    }

    /**
     * AD
     */

    private int mReloadCount = 1;
    private NativeExpressAD2 mADManager;
    private NativeExpressADData2 mNativeExpressADData2;

    private void initNativeExpressAD2() {
        mADManager = new NativeExpressAD2(getContext(), Constants.EXPRESS_POS_ID, this);
        mADManager.loadAd(1);
    }

    @Override
    public void onLoadSuccess(List<NativeExpressADData2> adDataList) {
        Log.i(TAG, "onLoadSuccess: dataSize = " + adDataList.size());
        processAdData(adDataList);
    }

    /**
     * 因为模板2.0 的广告是渲染成功后，才有广告的 View，进而添加到UI中显示。
     * 如果多条广告同时开始渲染，渲染成功的回调顺序是不确定的，有可能第 2 条先渲染成功，然后第 1 条才渲染成功，
     * 这样导致的结果可能就是列表被滚动到第 2 条广告第位置，但其实用户并没有滑动。
     * 所以这里采用一条一条的渲染广告的方式，当前广告渲染成功或失败后再去渲染下一条广告。
     */
    private void processAdData(List<NativeExpressADData2> adDataList) {
        if (adDataList.size() > 0) {
            mNativeExpressADData2 = adDataList.get(0);
            mNativeExpressADData2.setAdEventListener(new AdEventListener() {
                @Override
                public void onClick() {
                    Log.i(TAG, "onClick: " + mNativeExpressADData2);
                }

                @Override
                public void onExposed() {
                    Log.i(TAG, "onImpression: " + mNativeExpressADData2);
                }

                @Override
                public void onRenderSuccess() {
                    Log.i(TAG, "onRenderSuccess: " + mNativeExpressADData2);
                    mAdapter.changeAD(mNativeExpressADData2);
                }

                @Override
                public void onRenderFail() {
                    Log.i(TAG, "onRenderFail: " + mNativeExpressADData2);
                }

                @Override
                public void onAdClosed() {
                    Log.i(TAG, "onAdClosed: " + mNativeExpressADData2);
                    mAdapter.changeAD(mNativeExpressADData2);
                    mNativeExpressADData2.destroy();
                }
            });

            mNativeExpressADData2.setMediaListener(new MediaEventListener() {
                @Override
                public void onVideoCache() {
                    Log.i(TAG, "onVideoCache: " + mNativeExpressADData2);
                }

                @Override
                public void onVideoStart() {
                    Log.i(TAG, "onVideoStart: " + mNativeExpressADData2);
                }

                @Override
                public void onVideoResume() {
                    Log.i(TAG, "onVideoResume: " + mNativeExpressADData2);
                }

                @Override
                public void onVideoPause() {
                    Log.i(TAG, "onVideoPause: " + mNativeExpressADData2);
                }

                @Override
                public void onVideoComplete() {
                    Log.i(TAG, "onVideoComplete: " + mNativeExpressADData2);
                }

                @Override
                public void onVideoError() {
                    Log.i(TAG, "onVideoError: " + mNativeExpressADData2);
                }
            });

            mNativeExpressADData2.render();
        }
    }

    @Override
    public void onNoAD(AdError error) {
        Log.i(TAG, String.format("onNoAD: error code : %d, error msg %s", error.getErrorCode(),
                error.getErrorMsg()));
        if (mReloadCount <= 3) {
            mReloadCount++;
            mADManager.loadAd(1);
        }
    }
}
