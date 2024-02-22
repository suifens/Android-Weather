package com.goodtech.tq.modules.others.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.goodtech.tq.base.BaseWebActivity;
import com.goodtech.tq.utils.TipHelper;

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

        if (!TextUtils.isEmpty(mUrl)) {
            TipHelper.showProgressDialog(this);
            mWebView.loadUrl(mUrl);
        }
    }

}