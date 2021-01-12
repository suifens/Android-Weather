package com.goodtech.tq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.MainActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.fragment.adapter.WeatherRecyclerAdapter;
import com.goodtech.tq.httpClient.ApiCallback;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.news.NewsActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.Objects;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment extends BaseFragment implements OnRefreshListener {

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
}
