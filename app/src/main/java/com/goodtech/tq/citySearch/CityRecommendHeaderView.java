package com.goodtech.tq.citySearch;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.models.CityMode;

public class CityRecommendHeaderView extends LinearLayout {

    private CityRecommendAdapter.OnItemClickListener mListener;
    private TextView mLocationTv;
    private CityMode mCityMode;
    private View mLocationView;
    private View mSearchView;
    protected RippleLayout mRippleLayout;

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

    public void onStart() {
        if (mSearchView.getVisibility() == VISIBLE) {
            mRippleLayout.startRippleAnimation();
        }
    }

    public void onStop() {
        mRippleLayout.stopRippleAnimation();
    }

    /**
     * 初始化参数
     */
    private void init(Context context, AttributeSet attrs) {

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View view = LayoutInflater.from(context).inflate(R.layout.search_header_recommend, this, true);

        mLocationView = view.findViewById(R.id.layout_location_container);
        mSearchView = view.findViewById(R.id.layout_location_start);
        mSearchView.setOnClickListener(v -> onLocationClick());

        mLocationTv = view.findViewById(R.id.tv_location);
        view.findViewById(R.id.layout_location).setOnClickListener(v -> onLocationClick());
        view.findViewById(R.id.btn_location).setOnClickListener(v-> onLocationClick());
        view.findViewById(R.id.layout_refresh).setOnClickListener(v-> onLocationClick());

        mRippleLayout = view.findViewById(R.id.ripplelayout_train_main);

        updateLocation();
    }

    private void onLocationClick() {
        if (mListener != null) {
            mListener.onItemClick(mLocationTv, 0, mCityMode);
        }
    }

    public void updateLocation() {
        CityMode location = LocationSpHelper.getLocation();
        this.mCityMode = location;
        if (location != null) {
            if (mLocationTv != null && !TextUtils.isEmpty(location.getMergerName())) {
                mLocationTv.setText(location.getMergerName());
            }
            mSearchView.setVisibility(GONE);
            mLocationView.setVisibility(VISIBLE);
        } else {
            mSearchView.setVisibility(VISIBLE);
            mLocationView.setVisibility(GONE);
        }
    }

    public void hideSoftInput(Activity activity) {
        if (mLocationTv != null) {
            //  隐藏键盘
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLocationTv.getWindowToken(), 0);
        }
    }

    public void setListener(CityRecommendAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }
}
