package com.goodtech.tq.modules.others.dymovies;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.utils.TipHelper;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;

public class DyMoviesActivity extends BaseActivity {

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected boolean firstLoad = true;

    protected WebView mWebView;
    protected String mUrl = "https://iring.diyring.cc/friend/7adfedeaef801c3d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typhoon);

        configStationBar(findViewById(R.id.private_station_bar));
        findViewById(R.id.button_back).setOnClickListener(v -> {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
        });

        TextView mTextView = (TextView) findViewById(R.id.titleTv);
        mTextView.setText("抖音热门彩铃");

        mWebView = findViewById(R.id.web_activity);

        configWebView(mWebView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                TipHelper.dismissProgressDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("Ac_Movies");
        MobclickAgent.onResume(this);
        mHandler.postDelayed(() -> {
            if (!TextUtils.isEmpty(mUrl)) {
                if (mWebView.getUrl() != null) {
                    mWebView.reload();
                } else {
                    TipHelper.showProgressDialog(this);
                    mWebView.loadUrl(mUrl);
                }
            }
        }, 500);

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Ac_Movies");
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        //关闭webview取消注册
        if (mWebView != null) {
            mWebView.clearHistory();
//
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView.clearCache(true);
            mWebView = null;
        }
        //清理Webview缓存数据库
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
//        webSettings.setAppCacheEnabled(true);
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

//        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setLongClickable(true);
        webView.getSettings().setTextZoom(100);
        webView.setOnLongClickListener(v -> true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                TipHelper.dismissProgressDialog();
            }
        });
    }
}