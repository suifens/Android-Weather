package com.goodtech.tq.base.share;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.blankj.utilcode.util.AppUtils;
import com.goodtech.tq.R;
import com.goodtech.tq.app.App;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.TipHelper;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
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
        
        mIWXAPI = WXAPIFactory.createWXAPI(activity, Constants.WECHAT_APP_ID);

        mTencent = Tencent.createInstance(Constants.QQ_APP_ID, activity, APP_AUTHORITIES);
    }

    public void sharedLink(String urlString,
                           ShareType type,
                           String title,
                           String des,
                           ShareHelperCallback shareHelperCallback)
    {
        this.mShareHelperCallback = shareHelperCallback;
        switch (type) {
            case WeChat:
            case WeChatMoments:
                int targetScene = type == ShareType.WeChat ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
                sharedLinkToWX(urlString, title, des, targetScene);
                break;
            case QQ:
                sharedLinkToQQ(urlString, title, des);
                break;
            case Weibo:
                sharedLinkToWeibo(urlString);
                break;
            case More:
                sharedLinkToMore(mActivity, urlString);
                break;
        }
    }

    private void sharedLinkToWX(String url, String title, String des, int targetScene) {
        if (!mIWXAPI.isWXAppInstalled()) {
            TipHelper.dismissProgressDialog();
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = des;;
        Bitmap bmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ic_about_logo);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 318, 568, true);
        msg.thumbData = bmpToByteArray(thumbBmp, false);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("url");
        req.message = msg;
        req.scene = targetScene;
        mIWXAPI.sendReq(req);
        bmp.recycle();
    }

    private void sharedLinkToQQ(String lineUrl, String title, String des) {

        if (!mTencent.isQQInstalled(mActivity)) {
            TipHelper.dismissProgressDialog();
            return;
        }
        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, lineUrl);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, lineUrl);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, AppUtils.getAppName());
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        mTencent.shareToQQ(mActivity, params, null);
    }

    public void sharedLinkToWeibo(String lineUrl) {

//        if (!DeviceUtil.isWeiboInstall(mActivity)) {
//            TipHelper.dismissProgressDialog();
//            TipHelper.showWarnTip(mActivity, R.string.weibo_install_error);
//            return;
//        }
//
//        if (mWbShareHandler == null) {
//            mWbShareHandler = new WbShareHandler(mActivity);
//            mWbShareHandler.registerApp();
//        }
//
//        WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
//        TextObject textObject = new TextObject();
//        textObject.text = lineUrl;
//        weiboMultiMessage.textObject = textObject;
//        mWbShareHandler.shareMessage(weiboMultiMessage, true);
    }

    public void shareToMore(Activity activity, String imgUrl) {
        Intent imageIntent = new Intent(Intent.ACTION_SEND);
        imageIntent.setType("image/*");
        imageIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imgUrl));
        activity.startActivity(Intent.createChooser(imageIntent, "分享"));
    }

    public void sharedLinkToMore(Activity activity, String lineUrl) {
        Intent imageIntent = new Intent(Intent.ACTION_SEND);
        imageIntent.setType("text/plain");
        imageIntent.putExtra(Intent.EXTRA_TEXT, lineUrl);
        activity.startActivity(Intent.createChooser(imageIntent, "分享链接"));
    }

    public boolean shareToQQ(String imgUrl, IUiListener listener) {

        if (!mTencent.isQQInstalled(App.instance)) {
            TipHelper.dismissProgressDialog();
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
            TipHelper.dismissProgressDialog();
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
