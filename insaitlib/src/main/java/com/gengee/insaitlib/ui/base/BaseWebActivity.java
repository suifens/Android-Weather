package com.gengee.insaitlib.ui.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.BarUtils;
import com.gengee.insaitlib.R;
import com.gengee.insaitlib.ui.dialog.LoadingDialog;

import java.lang.reflect.Method;

public abstract class BaseWebActivity<T extends ViewBinding> extends AppCompatActivity implements CreateInit<T> {

    private FrameLayout layout_content; // 子类view容器

    protected Context context;

    private LoadingDialog loadingDialog;

    protected T mBinding;

    @Override
    protected void onPause() {
        super.onPause();
        dismissLoading();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.setContentView(R.layout.activity_base);
        context = this;
        init();
    }

    protected void init() {
        layout_content = super.findViewById(R.id.frameLayout);

        mBinding = bindView();
        setContentView(mBinding.getRoot());

        // 初始化数据
        prepareData(getIntent());
        // 初始化view
        initView();
        // 初始化事件
        initEvent();
        // 加载数据
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 初始化数据
        prepareData(intent);
    }

    public void setContentView(View view) {
        layout_content.removeAllViews();
        layout_content.addView(view);
    }

    /**
     * 配置station bar
     */
    public void configStationBar(View stationBar) {
        ConstraintLayout.LayoutParams bars = new ConstraintLayout.LayoutParams(stationBar.getLayoutParams());
        bars.height = bars.height + BarUtils.getStatusBarHeight();
        stationBar.setLayoutParams(bars);

        immersionStatusBar();
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void configWebView(WebView webView) {
        WebSettings webSettings = webView.getSettings();

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        //  打开本地缓存
        webSettings.setDomStorageEnabled(true);
        //不缓存
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setSupportZoom(true);
        try {
            Class<?> clazz = webSettings.getClass();
            Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
            method.invoke(webSettings, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setLongClickable(true);
        webView.getSettings().setTextZoom(100);
        webView.setOnLongClickListener(v -> true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                saveToWebLocalStorage(webView);
                super.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dismissLoading();
            }
        });
    }

    protected void saveToWebLocalStorage(WebView webView) {

    }

    /**
     * 沉浸式状态栏
     */
    protected void immersionStatusBar() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 沉浸式状态栏
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        // window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//实现状态栏图标和文字颜色为暗色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // 状态栏改为透明
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void dismissLoading() {
        if (loadingDialog != null) {
            showLoading(false, null);
        }
    }

    protected void dismissLoading(long delay) {
        new Handler().postDelayed(() -> showLoading(false, null), delay);
    }

    protected void showLoading() {
        showLoading(true, null);
    }

    protected void showLoading(boolean show, String tip) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        if (show) {
            if (tip != null && !tip.isEmpty()) {
                loadingDialog.setTip(tip);
            } else {
                loadingDialog.setTip("请稍后...");
            }
            loadingDialog.show();
        } else {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

}
