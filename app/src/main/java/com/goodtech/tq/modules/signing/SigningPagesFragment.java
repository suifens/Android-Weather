package com.goodtech.tq.modules.signing;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.BaseFragment;
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter;
import com.goodtech.tq.utils.DeviceUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

/**
 * 签到页面
 */
public class SigningPagesFragment extends BaseFragment {

    protected ViewPager2 mViewPager;
    protected RadioGroup mRgIndicator;

    protected int mCurrentPageIndex;

    protected List<Fragment> mPagerViews;
    protected RxPermissions mRxPermissions;

    protected void setPagerViews(List<Fragment> pagerViews) {
        this.mPagerViews = pagerViews;

        if (pagerViews.size() > 1) {
            mRgIndicator.setVisibility(View.VISIBLE);
        } else {
            mRgIndicator.setVisibility(View.GONE);
        }

        ViewPagerAdapter trainsPageAdapter = new ViewPagerAdapter(getActivity(), mPagerViews);
        mViewPager.setAdapter(trainsPageAdapter);

        int width = DeviceUtils.dip2px(getContext(), 8);
        int height = DeviceUtils.dip2px(getContext(), 3);
        int margin = DeviceUtils.dip2px(getContext(), 6);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(width, height);
        for (int i = 0; i < mPagerViews.size(); i++) {
            RadioButton tempButton = new RadioButton(getActivity());
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
        setIndicator(mCurrentPageIndex);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRxPermissions = new RxPermissions(requireActivity()); // where this is an Activity instance

        mViewPager.registerOnPageChangeCallback(mOnPageChangeListener);
    }


    protected void setIndicator(int position) {
        if (position >= 0 && position < mRgIndicator.getChildCount()) {
            mRgIndicator.check(mRgIndicator.getChildAt(position).getId());
        }
    }

    protected void pageSelected(int position) {

    }

    protected ViewPager2.OnPageChangeCallback mOnPageChangeListener = new ViewPager2.OnPageChangeCallback() {

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (mCurrentPageIndex != position) {
                mCurrentPageIndex = position;
                setIndicator(mCurrentPageIndex);
                pageSelected(position);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPagerViews != null && !mPagerViews.isEmpty()) {
            mPagerViews.clear();
            mPagerViews = null;
        }

        mOnPageChangeListener = null;
        mViewPager = null;
    }
}
