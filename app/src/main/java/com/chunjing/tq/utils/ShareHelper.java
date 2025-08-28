package com.chunjing.tq.utils;

import static com.chunjing.tq.ext.CustomViewExtKt.WECHAT_APP_ID;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.AppUtils;
import com.chunjing.tq.MyApp;
import com.chunjing.tq.ext.ShareType;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.ByteArrayOutputStream;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class ShareHelper {
    public final static String TAG = ShareHelper.class.getSimpleName();

    private static final String APP_AUTHORITIES = "com.tencent.sample.fileprovider";

    private Activity mActivity;

    public static final int SHARE_REQUEST_CODE = 0x111;

    private ShareHelperCallback mShareHelperCallback;
    private IWXAPI mIWXAPI;

    private Tencent mTencent;

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    
    public ShareHelper(Activity activity) {
        
        this.mActivity = activity;
        
        mIWXAPI = WXAPIFactory.createWXAPI(activity, WECHAT_APP_ID);

        mTencent = Tencent.createInstance("1112185117", activity, APP_AUTHORITIES);
    }

    public boolean shareToQQ(String imgUrl) {

        if (!mTencent.isQQInstalled(MyApp.Companion.instance())) {
            return false;
        }
        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imgUrl);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, AppUtils.getAppName());
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare. SHARE_TO_QQ_TYPE_IMAGE);
        mTencent.shareToQQ(mActivity, params, new IUiListener() {
            @Override
            public void onComplete(Object o) {

            }

            @Override
            public void onError(UiError uiError) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onWarning(int i) {

            }
        });
        return true;
    }
    
    public boolean shareImgToWx(Bitmap bitmap, int targetScene) {
        if (!mIWXAPI.isWXAppInstalled()) {
            return false;
        }
        WXImageObject wxImageObject = new WXImageObject(bitmap);
        WXMediaMessage wxMediaMessage = new WXMediaMessage();
        wxMediaMessage.mediaObject = wxImageObject;
//
//        //设置缩略图
//        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
//        bitmap.recycle();
//        wxMediaMessage.thumbData = bmpToByteArray(thumbBmp, true);
        
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = wxMediaMessage;
        req.scene = targetScene;
        mIWXAPI.sendReq(req);
        return true;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    protected static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void setShareHelperCallback(ShareHelperCallback shareHelperCallback) {
        mShareHelperCallback = shareHelperCallback;
    }

    public interface ShareHelperCallback {
        void onSuccess(boolean isSuccess, int errorResId);
        
        void onCancel();
        
    }
}
