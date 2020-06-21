package com.goodtech.tq.citySearch.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CityRecyclerAdapter;
import com.goodtech.tq.models.CityMode;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class CityRecommendHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CityRecyclerAdapter.OnItemClickListener mListener;
    private TextView mRecommendTv;
    private CityMode mCityMode;

    public CityRecommendHolder(View view, CityRecyclerAdapter.OnItemClickListener listener) {
        super(view);
        mRecommendTv = view.findViewById(R.id.tv_recommend);
        mListener = listener;
        view.setOnClickListener(this);
    }

    public static int resource() {
        return R.layout.search_item_recommend;
    }

    public void setCityMode(CityMode mode) {

        this.mCityMode = mode;
        mRecommendTv.setText(mode.city);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClick(v, getPosition(), mCityMode);
        }
    }
}
