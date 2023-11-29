package com.goodtech.tq.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.base.share.ShareHelper;
import com.goodtech.tq.base.share.ShareType;
import com.goodtech.tq.utils.ImageTools;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.popup.TopTitlePopup;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;

@SuppressLint("NonConstantResourceId")
public class BaseShareActivity extends BaseActivity {

    private static final String TAG = "BaseShareActivity";

    protected RxPermissions mRxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRxPermissions = new RxPermissions(this); // where this is an Activity instance
    }

    /**
     * 分享
     */
    protected ShareHelper mShareHelper;
    protected String mTrainId;

    @Override
    protected void onResume() {
        super.onResume();
        if (mShareImgPath != null) {
            FileUtils.delete(mShareImgPath);
        }
        TipHelper.dismissProgressDialog();
    }

    @SuppressLint("CheckResult")
    protected void onShareTypePressed(final ShareType shareType) {
        if (mRxPermissions == null) {
            return;
        }
        if (mRxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            startShare(shareType);
        } else {
            TopTitlePopup popup = new TopTitlePopup(this);
            popup.setupData("请允许天气预报使用读写权限", "使用分享功能，我们需要将您的图片先储存手机文件中，如果您拒绝，也不会影响您使用产品的其他功能");
            BasePopupView popupView = new XPopup.Builder(this)
                    .isDestroyOnDismiss(true)
                    .popupPosition(PopupPosition.Top)
                    .hasShadowBg(false)
                    .hasStatusBarShadow(false)
                    .asCustom(popup);
            popupView.show();

            mRxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        popupView.dismiss();
                        if (granted) {
                            startShare(shareType);
                        }
                    });
        }
    }

    protected void startShare(final ShareType shareType) {

        if (mShareHelper == null) {
            mShareHelper = new ShareHelper(BaseShareActivity.this);
            mShareHelper.setShareHelperCallback(mShareHelperCallback);
        }

        TipHelper.showProgressDialog(this, R.string.tip_shotting, false);

        getScreenShotBitmap();

        new Handler().postDelayed(() -> {
            if (saveBitmap != null) {
                saveBitmap = ImageTools.compressImage(saveBitmap);
                shareImage(saveBitmap, shareType);
            } else {
//                TipHelper.showWarnTip(this, "截图失败");
            }
        }, 500);
    }

    protected String mShareImgPath;

    protected void shareImage(Bitmap saveBitmap, ShareType shareType) {
        if (mShareHelper == null) {
            mShareHelper = new ShareHelper(BaseShareActivity.this);
            mShareHelper.setShareHelperCallback(mShareHelperCallback);
        }
        boolean callShare = false;
        switch (shareType) {
            case WeChat:
                callShare = mShareHelper.shareImgToWx(saveBitmap, SendMessageToWX.Req.WXSceneSession);
                break;
            case WeChatMoments:
                callShare = mShareHelper.shareImgToWx(saveBitmap, SendMessageToWX.Req.WXSceneTimeline);
                break;
            case QQ:
                mShareImgPath = ImageTools.saveImageToGallery(BaseShareActivity.this, saveBitmap);
                callShare = mShareHelper.shareToQQ(mShareImgPath, null);
                break;
            case More:
                mShareImgPath = ImageTools.saveImageToGallery(BaseShareActivity.this, saveBitmap);
                mShareHelper.shareToMore(BaseShareActivity.this, mShareImgPath);
                callShare = true;
                break;
            case Save:
                String path = ImageTools.saveImageToGallery(BaseShareActivity.this, saveBitmap);
                callShare = !TextUtils.isEmpty(path);
                TipHelper.dismissProgressDialog();
                Toast.makeText(this, callShare ? "保存成功" : "保存失败", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    protected ShareHelper.ShareHelperCallback mShareHelperCallback = new ShareHelper.ShareHelperCallback() {
        @Override
        public void onSuccess(final boolean isSuccess, final int errorResId) {
            Log.e(TAG, "onSuccess: ");
            TipHelper.dismissProgressDialog();
            mHandler.post(() -> {
                if (!isSuccess && errorResId != 0) {
//                    TipHelper.showWarnTip(BaseShareActivity.this, errorResId);
                }
            });
        }

        @Override
        public void onCancel() {
            Log.e(TAG, "onCancel: ");
            TipHelper.dismissProgressDialog();
        }
    };

    protected Bitmap saveBitmap;

    //  截屏
    protected void screenshot() {

        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }

        TipHelper.showProgressDialog(BaseShareActivity.this, R.string.tip_shotting, false);

        getScreenShotBitmap();

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (saveBitmap != null) {
                try {
                    //  图片保存到照片中
                    ImageTools.saveImageToGallery(this, saveBitmap);

                    TipHelper.dismissProgressDialog();
//                    TipHelper.showTip(BaseShareActivity.this, R.string.tip_save_success);

                } catch (Exception e) {
                    e.printStackTrace();
//                    TipHelper.showTip(BaseShareActivity.this, R.string.tip_save_failure);
                }
            } else {
//                TipHelper.showTip(BaseShareActivity.this, R.string.tip_save_failure);
            }
        }, 500);
    }

    protected void getScreenShotBitmap() {

    }
}
