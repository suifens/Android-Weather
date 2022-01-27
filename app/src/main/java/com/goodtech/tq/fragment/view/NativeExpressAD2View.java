package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.qq.e.ads.nativ.NativeExpressADView;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class NativeExpressAD2View extends FrameLayout {

    public NativeExpressAD2View(Context context) {
        this(context, null);
    }

    public NativeExpressAD2View(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public NativeExpressAD2View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    FrameLayout mAdContainer;
    
    @SuppressLint("DefaultLocale")
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_ad, this, true);
        mAdContainer = view.findViewById(R.id.express_2_ad_container);
    }

//    public void setData(NativeExpressADData2 adData) {
//        if (adData == null || adData.getAdView() == null) {
//            return;
//        }
//
//        View adView = adData.getAdView();
//        if (mAdContainer.getChildCount() > 0 && mAdContainer.getChildAt(0) == adView) {
//            return;
//        }
//        if (mAdContainer.getChildCount() > 0) {
//            mAdContainer.removeAllViews();
//        }
//        if (adView != null && adView.getParent() != null) {
//            ((ViewGroup) adView.getParent()).removeView(adView);
//        }
//        mAdContainer.addView(adView);
//    }

    public void setAdView(NativeExpressADView adView) {

        if (adView == null) {
            return;
        }

        if (mAdContainer.getChildCount() > 0
                && mAdContainer.getChildAt(0) == adView) {
            return;
        }

        if (mAdContainer.getChildCount() > 0) {
            mAdContainer.removeAllViews();
        }

        if (adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }

        mAdContainer.addView(adView);
        adView.render(); // 调用render方法后sdk才会开始展示广告
    }

}
