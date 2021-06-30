package com.goodtech.tq.views;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;

/**
 * 公用的弹出框
 */
public class LoadingDialog extends ProgressDialog {

    private AnimationDrawable mAnimation;
    private Context mContext;
    private ImageView mImageView;
    private String mLoadingTitle;
    private TextView mLoadingTv;

    public LoadingDialog(Context context) {
        super(context, R.style.loading_dialog);
        initView(context, "加载中...");
    }

    public LoadingDialog(Context context, String content) {
        super(context, R.style.loading_dialog);
        initView(context, content);
    }

    private void initView(Context context, String content) {
        this.mContext = context;
        this.mLoadingTitle = content;
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {

        mImageView.setBackgroundResource(R.drawable.anim_loading);
        mAnimation = (AnimationDrawable) mImageView.getBackground();
        mImageView.post(() -> mAnimation.start());
        mLoadingTv.setText(mLoadingTitle);

    }

    public void setContent(String str) {
        mLoadingTv.setText(str);
    }

    private void initView() {
        setContentView(R.layout.progress_dialog_loading);
        mLoadingTv = (TextView) findViewById(R.id.loadingTv);
        mImageView = (ImageView) findViewById(R.id.loadingIv);
    }

}
