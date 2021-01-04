package com.goodtech.tq.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class TipHelper {
    private static final String TAG = "TipHelper";
    protected static Handler mHandler = new Handler(Looper.getMainLooper());
    
    public static void makeShortToast(Context ctx, int cs) {
        Toast.makeText(ctx, cs, Toast.LENGTH_SHORT).show();
    }
    
    public static void makeTopShortToast(Context ctx, int cs) {
        Toast toast = Toast.makeText(ctx, cs, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }
    
    public static void makeLongToast(Context ctx, int cs) {
        Toast.makeText(ctx, cs, Toast.LENGTH_LONG).show();
    }
    
    private static ProgressDialog mProgressDialog;

    public synchronized static boolean isShowing() {
        if (mProgressDialog != null) {
            return mProgressDialog.isShowing();
        }
        return false;
    }
    
    public synchronized static void showProgressDialog(final Activity context, final int resId, final boolean cancelable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
    
                mProgressDialog = new ProgressDialog(context);
    
                mProgressDialog.setMessage(context.getString(resId));
                mProgressDialog.setCancelable(cancelable);
                try {
                    if (!context.isFinishing()) {
                        mProgressDialog.show();
                    }
                } catch (Exception e) {
                    mProgressDialog = null;
                    e.printStackTrace();
                }
            }
        });
        
    }

    public synchronized static void showProgressDialog(final Activity context, final boolean cancelable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();

                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setCancelable(cancelable);
                try {
                    if (!context.isFinishing()) {
                        mProgressDialog.show();
                    }
                } catch (Exception e) {
                    mProgressDialog = null;
                    e.printStackTrace();
                }
            }
        });

    }

    public synchronized static void showProgressDialog(final Activity context) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();

                mProgressDialog = new ProgressDialog(context);
                try {
                    if (!context.isFinishing()) {
                        mProgressDialog.show();
                    }
                } catch (Exception e) {
                    mProgressDialog = null;
                    e.printStackTrace();
                }
            }
        });

    }
    
    
    public static void dismissProgressDialog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
            }
        });
        
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
