package com.gengee.insaitlib.ui.view.tip;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gengee.insaitlib.R;


public class MyToast implements TipRelativeLayout.AnimationEndCallback {
    
    private PopupWindow reportVideoPopwindow;
    
    public enum TipType {
        Message, Warn, Error;
    }
    
    protected TextView mContentTv;
    
    public void showTips(Activity context, int resId, TipType tipType) {
        initData(context, tipType);
        mContentTv.setText(resId);
        
    }
    
    public void showTips(Activity context, String tip, TipType tipType) {
        initData(context, tipType);
        mContentTv.setText(tip);
        
    }
    
    protected void initData(Activity context, TipType tipType) {
        View parent = ((ViewGroup) (context).findViewById(android.R.id.content)).getChildAt(0);
        View popView = LayoutInflater.from(context).inflate(R.layout.view_popupwindow_tips, null);
        reportVideoPopwindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        reportVideoPopwindow.showAtLocation(parent, Gravity.TOP, 0, 0);
        TipRelativeLayout tvTips = (TipRelativeLayout) popView.findViewById(R.id.rl_tips);
        tvTips.setAnimationEnd(this);//设置动画结束监听函数
        tvTips.showTips();//显示提示RelativeLayout,移动动画.
        View bgLayout = popView.findViewById(R.id.rl_tips);
        ImageView icon = (ImageView) popView.findViewById(R.id.iv_image);
        mContentTv = (TextView) popView.findViewById(R.id.tv_pop_tip_content);
        icon.setVisibility(View.GONE);
        if (tipType == TipType.Warn) {
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_notice_s);
            bgLayout.setBackgroundResource(R.drawable.bg_red_tip);
            mContentTv.setTextColor(Color.WHITE);
        }
    }
    
    @Override
    public void onAnimationEnd() {
        if (reportVideoPopwindow != null) {
            reportVideoPopwindow.dismiss();//动画结束，隐藏popupwindow
        }
    }
}
