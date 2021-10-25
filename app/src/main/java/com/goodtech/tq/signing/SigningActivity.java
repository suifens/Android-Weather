package com.goodtech.tq.signing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.utils.DeviceUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

public class SigningActivity extends BaseActivity {

    private static final String EXTRA_WEATHER_HOURLY = "weather_hourly";
    private static final String EXTRA_CITY = "city";
    private static final String EXTRA_CONTINUOUS = "continuous";

    public static void redirectTo(Context ctx, Hourly model, CityMode city, int continuous) {
        Intent intent = new Intent(ctx, SigningActivity.class);
        intent.putExtra(EXTRA_CITY, city);
        intent.putExtra(EXTRA_WEATHER_HOURLY, model);
        intent.putExtra(EXTRA_CONTINUOUS, continuous);
        ctx.startActivity(intent);
    }

    protected ViewPager2 mViewPager;
    protected RadioGroup mRgIndicator;
    protected int mCurrentPageIndex;
    protected List<Fragment> mFragments;
    protected RxPermissions mRxPermissions;
    protected Hourly mHourly;
    protected CityMode mCityMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        mRxPermissions = new RxPermissions(this); // where this is an Activity instance

        mHourly = getIntent().getParcelableExtra(EXTRA_WEATHER_HOURLY);
        mCityMode = getIntent().getParcelableExtra(EXTRA_CITY);
        //  持续天数
        int continuous = getIntent().getIntExtra(EXTRA_CONTINUOUS, 0);

        //  设置滑动回调
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (mCurrentPageIndex != position) {
                    mCurrentPageIndex = position;
                    setIndicator(mCurrentPageIndex);
                }
            }
        });

        //  fragment列表配置
        List<Fragment> fragments = new ArrayList<>();
        addFragments(fragments, true);
        addFragments(fragments, false);
        for (Fragment fragment: fragments) {
            ((SigningFragment) fragment).updateData(mHourly, mCityMode, continuous);
        }

        setPagerViews(fragments);
    }

    /**
     * 配置 pager
     */
    protected void setPagerViews(List<Fragment> pagerViews) {

        this.mFragments = pagerViews;

        if (pagerViews.size() > 1) {
            mRgIndicator.setVisibility(View.VISIBLE);
        } else {
            mRgIndicator.setVisibility(View.GONE);
        }

        ViewPagerAdapter trainsPageAdapter = new ViewPagerAdapter(this, mFragments);
        mViewPager.setAdapter(trainsPageAdapter);

        int width = DeviceUtils.dip2px(this, 8);
        int height = DeviceUtils.dip2px(this, 3);
        int margin = DeviceUtils.dip2px(this, 6);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(width, height);
        for (int i = 0; i < 3; i++) {
            RadioButton tempButton = new RadioButton(this);
            tempButton.setEnabled(false);
            tempButton.setChecked(false);
            tempButton.setBackgroundResource(R.drawable.sl_rdobtn_guide_page);   // 设置RadioButton的背景图片
            tempButton.setButtonDrawable(null);
            if (i > 0) {
                layoutParams.leftMargin = margin;
            }
            mRgIndicator.addView(tempButton, layoutParams);
        }

        mViewPager.setCurrentItem(mCurrentPageIndex);
        setIndicator(mCurrentPageIndex % 3);
    }

    protected void setIndicator(int position) {
        if (position >= 0 && position < mRgIndicator.getChildCount()) {
            mRgIndicator.check(mRgIndicator.getChildAt(position).getId());
        }
    }

    /**
     * 添加fragment
     * @param fragments 列表
     * @param am 是否为早晨
     */
    private void addFragments(List<Fragment> fragments, boolean am) {
        SigningAFragment aFragment = new SigningAFragment();
        aFragment.configData(am);
        fragments.add(aFragment);

        SigningBFragment bFragment = new SigningBFragment();
        bFragment.configData(am);
        fragments.add(bFragment);

        SigningCFragment cFragment = new SigningCFragment();
        cFragment.configData(am);
        fragments.add(cFragment);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFragments != null && !mFragments.isEmpty()) {
            mFragments.clear();
            mFragments = null;
        }
        mViewPager = null;
    }
}