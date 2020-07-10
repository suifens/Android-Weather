package com.goodtech.tq.fragement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.goodtech.tq.R;
import com.goodtech.tq.fragement.adapter.WeatherRecyclerAdapter;
import com.goodtech.tq.models.WeatherModel;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment extends BaseFragment {

    private static final String TAG = "WeatherFragment";

    protected RecyclerView mRecyclerView;
    protected WeatherRecyclerAdapter mAdapter;
    protected WeatherModel mModel;

    protected View mStateBarBg;

    protected String mAddress;

    @Override
    protected int getViewLayoutRes() {
        return R.layout.fragment_item_list;
    }

    @Override
    protected void setupCacheViews() {
        super.setupCacheViews();
        mRecyclerView = (RecyclerView) mCacheView;
    }

    public void setStateBar(View stateBar) {
        this.mStateBarBg = stateBar;
    }

    private int totalDy = 0;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new WeatherRecyclerAdapter(getContext(), mModel, mAddress);
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
                    Log.d(TAG, "onScrolled: alpha = " + alpha);
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

    public void changeWeather(WeatherModel model, String address) {

        if (mModel != null && model.expireTime == mModel.expireTime) {
            return;
        }

        this.mModel = model;
        this.mAddress = address;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged(model, address);
        }
    }
}
