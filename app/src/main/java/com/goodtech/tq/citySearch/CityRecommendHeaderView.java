package com.goodtech.tq.citySearch;

import android.content.Context;
import android.content.res.TypedArray;
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

        int imgIcon = 0;
        String title = "";

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View view = LayoutInflater.from(context).inflate(R.layout.search_header_recommend, this, true);
        CityMode location = LocationSpHelper.getLocation();
        this.mCityMode = location;

        mLocationTv = view.findViewById(R.id.tv_location);
        if (location.cid != 0) {
            mLocationTv.setText(location.city);
        } else {
            mLocationTv.setText("定位失败");
        }
        mLocationTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(mLocationTv, 0, mCityMode);
                }
            }
        });
    }

    public void setListener(CityRecommendAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }
}
