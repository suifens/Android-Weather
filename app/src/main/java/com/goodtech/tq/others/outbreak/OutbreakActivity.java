package com.goodtech.tq.others.outbreak;

import android.os.Bundle;
import android.text.TextUtils;

import com.goodtech.tq.base.BaseWebActivity;
import com.goodtech.tq.utils.TipHelper;

public class OutbreakActivity extends BaseWebActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitleView.setText("疫情出行");
        mUrl = "file:///android_asset/chuxing/index.html";
        mChannel = "Ac_Outbreak";

        if (!TextUtils.isEmpty(mUrl)) {
            TipHelper.showProgressDialog(this);
            mWebView.loadUrl(mUrl);
        }
    }
}