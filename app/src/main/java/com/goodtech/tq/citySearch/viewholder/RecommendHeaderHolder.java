package com.goodtech.tq.citySearch.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CityRecommendAdapter;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.models.CityMode;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class RecommendHeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private CityRecommendAdapter.OnItemClickListener mListener;
    private TextView mLocationTv;
    private CityMode mCityMode;

    public RecommendHeaderHolder(View view, CityRecommendAdapter.OnItemClickListener listener) {
        super(view);

        CityMode location = LocationSpHelper.getLocation();
        this.mCityMode = location;

        mLocationTv = view.findViewById(R.id.tv_location);
        if (mLocationTv != null) {
            if (location.cid != 0) {
                mLocationTv.setText(location.city);
            } else {
                mLocationTv.setText("定位失败");
            }
            mLocationTv.setOnClickListener(this);
        }

        mListener = listener;
    }

    public static int resource() {
        return R.layout.search_header_recommend;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClick(v, getPosition(), mCityMode);
        }
    }
}
