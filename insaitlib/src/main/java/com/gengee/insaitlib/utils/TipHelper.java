package com.gengee.insaitlib.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.gengee.insaitlib.ui.view.tip.MyToast;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class TipHelper {
    private static final String TAG = "TipHelper";
    private static MyToast myToast;
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

    public synchronized static void showTip(final Context ctx, final String tip) {
        if (ctx == null || ((Activity)ctx).isFinishing()) {
            Log.d(TAG, "showTip() fail! ctx is null");
            return;
        }
        mHandler.post(() -> {
            try {
                if (myToast == null) {
                    myToast = new MyToast();
                } else {
                    myToast.onAnimationEnd();
                }
                myToast.showTips((Activity) ctx, tip, MyToast.TipType.Message);
            } catch (ClassCastException e) {
                Log.e(TAG, e.getMessage());
                myToast = null;
            }
        });
    }
    public synchronized static void showTip(final Context ctx, final int tip) {
        if (ctx == null || ((Activity)ctx).isFinishing()) {
            Log.d(TAG, "showTip() fail! ctx is null");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (myToast == null) {
                        myToast = new MyToast();
                    } else {
                        myToast.onAnimationEnd();
                    }
                    myToast.showTips((Activity) ctx, tip, MyToast.TipType.Message);
                } catch (ClassCastException e) {
                    Log.e(TAG, e.getMessage());
                    myToast = null;
                }
            }
        });
    }


    public synchronized static void showWarnTip(final Context ctx, final String tip) {
        if (ctx == null || ((Activity)ctx).isFinishing()) {
            Log.d(TAG, "showWarnTip() fail! ctx is null");
            return;
        }
        mHandler.post(() -> {
            try {
                if (myToast == null) {
                    myToast = new MyToast();
                } else {
                    myToast.onAnimationEnd();
                }
                myToast.showTips((Activity) ctx, tip, MyToast.TipType.Warn);
            } catch (ClassCastException e) {
                Log.e(TAG, e.getMessage());
                myToast = null;
            }
        });
    }

    public synchronized static void showWarnTip(final Context ctx,final int cs) {

        if (ctx == null || ((Activity)ctx).isFinishing()) {
            Log.d(TAG, "showWarnTip() fail! ctx is null");
            return;
        }
        mHandler.post(() -> {
            try {
                if (myToast == null) {
                    myToast = new MyToast();
                } else {
                    myToast.onAnimationEnd();
                }
                myToast.showTips((Activity) ctx, cs, MyToast.TipType.Warn);
            } catch (ClassCastException e) {
                Log.e(TAG, e.getMessage());
                myToast = null;
            }
        });
    }

    public synchronized static void dismissTip() {
        if (myToast != null) {
            myToast.onAnimationEnd();
        }
    }

}
