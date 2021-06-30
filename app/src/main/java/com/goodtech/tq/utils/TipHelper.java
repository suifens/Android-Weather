package com.goodtech.tq.utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;

import com.goodtech.tq.views.LoadingDialog;

public class TipHelper {
    protected static Handler mHandler = new Handler(Looper.getMainLooper());
    
    private static Dialog mProgressDialog;

    public synchronized static boolean isShowing() {
        if (mProgressDialog != null) {
            return mProgressDialog.isShowing();
        }
        return false;
    }
    
    public synchronized static void showProgressDialog(final Activity context, final int resId, final boolean cancelable) {
        if (context == null || context.isFinishing()) {
            return;
        }
        mHandler.post(() -> {
            dismissDialog();

            mProgressDialog = new LoadingDialog(context, context.getString(resId));
            mProgressDialog.setCancelable(cancelable);
            try {
                if (!context.isFinishing()) {
                    mProgressDialog.show();
                }
            } catch (Exception e) {
                mProgressDialog = null;
                e.printStackTrace();
            }
        });
        
    }

    public synchronized static void showProgressDialog(final Activity context, final boolean cancelable) {
        if (context == null || context.isFinishing()) {
            return;
        }
        mHandler.post(() -> {
            dismissDialog();

            mProgressDialog = new LoadingDialog(context);
            mProgressDialog.setCancelable(cancelable);
            try {
                if (!context.isFinishing()) {
                    mProgressDialog.show();
                }
            } catch (Exception e) {
                mProgressDialog = null;
                e.printStackTrace();
            }
        });

    }

    public synchronized static void showProgressDialog(final Activity context) {
        if (context == null || context.isFinishing()) {
            return;
        }
        mHandler.post(() -> {
            dismissDialog();

            mProgressDialog = new LoadingDialog(context);
            try {
                if (!context.isFinishing()) {
                    mProgressDialog.show();
                }
            } catch (Exception e) {
                mProgressDialog = null;
                e.printStackTrace();
            }
        });

    }
    
    
    public static void dismissProgressDialog() {
        mHandler.post(TipHelper::dismissDialog);
    }

    public static void dismissProgressDialog(long delayMillis) {
        mHandler.postDelayed(TipHelper::dismissDialog, delayMillis);
    }
    
    protected static void dismissDialog(){
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        } catch (Exception e) {
            mProgressDialog = null;
            e.printStackTrace();
        }
    }
    
}
