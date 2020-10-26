package com.goodtech.tq.citySearch;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.models.CityMode;

public class CityRecommendHeaderView extends LinearLayout {

    private CityRecommendAdapter.OnItemClickListener mListener;
    private TextView mLocationTv;
    private CityMode mCityMode;
    private TextView mTipTv;

    public CityRecommendHeaderView(Context context) {
        super(context);
        init(context, null);
    }

    public CityRecommendHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CityRecommendHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化参数
     */
    private void init(Context context, AttributeSet attrs) {


        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View view = LayoutInflater.from(context).inflate(R.layout.search_header_recommend, this, true);

        mLocationTv = view.findViewById(R.id.tv_location);
        view.findViewById(R.id.layout_location).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(mLocationTv, 0, mCityMode);
                }
            }
        });

        mTipTv = view.findViewById(R.id.tv_location_fail);

        updateLocation();
    }

    public void updateLocation() {
        CityMode location = LocationSpHelper.getLocation();
        this.mCityMode = location;
        if (location.cid != 0) {
            if (mLocationTv != null && !TextUtils.isEmpty(location.city)) {
                mLocationTv.setText(location.city);
            }
            mTipTv.setVisibility(GONE);
        } else {
            mLocationTv.setText("定位失败");
            mTipTv.setVisibility(VISIBLE);
        }
    }

    public void setListener(CityRecommendAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }
}
