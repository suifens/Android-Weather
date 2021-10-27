package com.goodtech.tq.wxapi;

import android.os.Bundle;
import android.util.Log;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.utils.Constants;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {
    
    public final static String TAG = "WXEntryActivity";
    
    public IWXAPI mIWXAPI;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIWXAPI = WXAPIFactory.createWXAPI(this, Constants.WECHAT_APP_ID, false);
        mIWXAPI.registerApp(Constants.WECHAT_APP_ID);
        mIWXAPI.handleIntent(getIntent(), this);
    }
    
    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "baseReq=" + baseReq);
    }
    
    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "微信回调:" + resp.transaction);
        String code = null;
        boolean isSuccess = false;
        String errorMsg = null;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK://授权成功
                isSuccess = true;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
            case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝
            case BaseResp.ErrCode.ERR_UNSUPPORT:
            default:
                isSuccess = false;
                break;
        }
        
        //授权登入
        if (resp instanceof SendAuth.Resp) {
//            EventBus.getDefault().post(new LoginActivity.LoginEvent(LoginType.WeChat)
//                    .setWxCode(((SendAuth.Resp) resp).code)
//                    .setSuccess(isSuccess)
//                    .setErrorMsg(getResources().getString(R.string.error_wx_auth)));
        } else if (resp instanceof SendMessageToWX.Resp) {//分享
            Log.e(TAG, "onResp: SendMessageToWX.Resp = " + resp);
//            EventBus.getDefault().post(new ShareEvent(ShareType.WeChat)
//                    .setSuccess(isSuccess)
//                    .setErrorMsg(getResources().getString(R.string.error_wx_share)));
        }
        
        finish();
    }
}
