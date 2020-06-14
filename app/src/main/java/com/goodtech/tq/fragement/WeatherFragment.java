package com.goodtech.tq.fragement;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goodtech.tq.R;
import com.goodtech.tq.fragement.adapter.WeatherRecyclerAdapter;
import com.goodtech.tq.models.WeatherModel;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment extends BaseFragment {

    protected RecyclerView mRecyclerView;
    protected WeatherRecyclerAdapter mAdapter;
    protected WeatherModel mModel;

    @Override
    protected int getViewLayoutRes() {
        return R.layout.fragment_item_list;
    }

    @Override
    protected void setupCacheViews() {
        super.setupCacheViews();
        mCacheView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_00c4ff));
        mRecyclerView = (RecyclerView) mCacheView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new WeatherRecyclerAdapter(getContext(), mModel);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void changeWeather(WeatherModel model, String address) {
        mModel = model;
        mAdapter.notifyDataSetChanged(model, address);
    }
}
