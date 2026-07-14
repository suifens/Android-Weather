package com.chunjing.tq.wxapi;

import static com.chunjing.tq.ext.CustomViewExtKt.WECHAT_APP_ID;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class WXEntryActivity extends FragmentActivity implements IWXAPIEventHandler {
    
    public final static String TAG = "WXEntryActivity";
    
    public IWXAPI mIWXAPI;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIWXAPI = WXAPIFactory.createWXAPI(this, WECHAT_APP_ID, false);
        mIWXAPI.registerApp(WECHAT_APP_ID);
        mIWXAPI.handleIntent(getIntent(), this);
    }
    
    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "baseReq=" + baseReq);
    }
    
    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "微信回调:" + resp.transaction + ", errCode=" + resp.errCode);
        finish();
    }
}
