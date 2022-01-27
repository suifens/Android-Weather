package com.goodtech.tq.fragment.viewholder;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.qq.e.ads.nativ.NativeExpressADView;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class NativeExpressAD2Holder extends RecyclerView.ViewHolder {

    FrameLayout mAdContainer;

    public NativeExpressAD2Holder(View view) {
        super(view);
        mAdContainer = view.findViewById(R.id.express_2_ad_container);
    }

    public static int getResource() {
        return R.layout.weather_item_ad;
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
