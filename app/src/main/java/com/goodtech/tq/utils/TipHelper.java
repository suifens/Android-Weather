package com.goodtech.tq.utils;

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
    
    public synchronized static void showProgressDialog(final Context context, final int resId, final boolean cancelable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
    
                mProgressDialog = new ProgressDialog(context);
    
                mProgressDialog.setMessage(context.getString(resId));
                mProgressDialog.setCancelable(cancelable);
                try {
                    mProgressDialog.show();
                } catch (WindowManager.BadTokenException badTokenException) {
                    mProgressDialog = null;
                } catch (Exception e) {
                    mProgressDialog = null;
                }
            }
        });
        
    }

    public synchronized static void showProgressDialog(final Context context, final boolean cancelable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();

                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setCancelable(cancelable);
                try {
                    mProgressDialog.show();
                } catch (WindowManager.BadTokenException badTokenException) {
                    mProgressDialog = null;
                } catch (Exception e) {
                    mProgressDialog = null;
                }
            }
        });

    }

    public synchronized static void showProgressDialog(final Context context) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();

                mProgressDialog = new ProgressDialog(context);
                try {
                    mProgressDialog.show();
                } catch (WindowManager.BadTokenException badTokenException) {
                    mProgressDialog = null;
                } catch (Exception e) {
                    mProgressDialog = null;
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
