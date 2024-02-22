package com.gengee.insaitlib.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.gengee.insaitlib.R;

public class BatteryBar extends LinearLayout {

    protected ProgressBar mProgressBar;
    protected ImageView mBgImgView;

    public BatteryBar(Activity context) {
        super(context);
        initData();
    }

    public BatteryBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public BatteryBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    protected void initData() {

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.view_battery, this, true);

        mProgressBar = rootView.findViewById(R.id.bar_battery_value);
        mProgressBar.setStartLeft(true);

        mBgImgView = rootView.findViewById(R.id.img_battery);
    }

    public void setLowProportion(float proportion) {
        if (mProgressBar != null) {
            mProgressBar.setLowProportion(proportion);
        }
    }

    public void setProcess(float value) {
        if (mProgressBar != null) {
            mProgressBar.setProcess(value);
        }
    }

    public void setMax(float max) {
        if (mProgressBar != null) {
            mProgressBar.setMax(max);
        }
    }

    public void setStartLeft(boolean startLeft) {
        if (mProgressBar != null) {
            mProgressBar.setStartLeft(startLeft);
        }
    }

    public void setLightType(boolean lightType) {
        if (lightType) {
            mBgImgView.setImageResource(R.drawable.ic_battery);
            mProgressBar.setSectionColors(new int[]{Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFF83F4F")});
        } else {
            mBgImgView.setImageResource(R.drawable.ic_battery_b);
            mProgressBar.setSectionColors(new int[]{Color.parseColor("#000000"),
                    Color.parseColor("#FFF83F4F")});
        }
    }
}
