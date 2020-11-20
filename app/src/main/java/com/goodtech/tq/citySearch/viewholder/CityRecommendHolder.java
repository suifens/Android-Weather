package com.goodtech.tq.citySearch.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CityRecommendAdapter;
import com.goodtech.tq.models.CityMode;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class CityRecommendHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CityRecommendAdapter.OnItemClickListener mListener;
    private TextView mRecommendTv;
    private CityMode mCityMode;

    public CityRecommendHolder(View view, CityRecommendAdapter.OnItemClickListener listener) {
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
        if (mode != null && !TextUtils.isEmpty(mode.city)) {
            mRecommendTv.setText(mode.city);
        }
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClick(v, getAdapterPosition(), mCityMode);
        }
    }
}
