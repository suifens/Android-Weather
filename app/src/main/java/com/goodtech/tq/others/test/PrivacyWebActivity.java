package com.goodtech.tq.others.test;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.goodtech.tq.R;
import com.goodtech.tq.base.BaseWebActivity;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.MessageAlert;

public class PrivacyWebActivity extends BaseWebActivity {

    private static final String EXTRA_LINK = "link";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_CHANNEL = "channel";

    public static void redirectTo(Context ctx, String link, String title, String channel) {
        Intent intent = new Intent(ctx, PrivacyWebActivity.class);
        intent.putExtra(EXTRA_LINK, link);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CHANNEL, channel);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitleView.setText(getIntent().getStringExtra(EXTRA_TITLE));
        mUrl = getIntent().getStringExtra(EXTRA_LINK);
        mChannel = getIntent().getStringExtra(EXTRA_CHANNEL);

        findViewById(R.id.button_back).setOnClickListener(v -> onBackClick());

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("TAG", " Exception is ==== >>> " + e);
                }
                return true;
            }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onBackClick() {
        if (mWebView.canGoBack()) {
            MessageAlert alert = new MessageAlert(this);
            alert.setCancelable(true);
            alert.setTitle(R.string.dialog_private_title);
            alert.setMessage("是否确定退出测试？");
            alert.setConfirmListener((dialog, which) -> {
                finish();
            });
            alert.show();
        } else {
            finish();
        }
    }

}