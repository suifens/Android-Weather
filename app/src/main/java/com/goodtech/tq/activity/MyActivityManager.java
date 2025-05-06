package com.goodtech.tq.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

import com.goodtech.tq.app.App;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class MyActivityManager {
    private static MyActivityManager sInstance = new MyActivityManager();
    // 采用弱引用持有 Activity ，避免造成 内存泄露
    private WeakReference<Activity> sCurrentActivityWeakRef;

    private MyActivityManager() {
    }
    
    public static MyActivityManager getInstance() {
        return sInstance;
    }
    
    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }
    
    public void setCurrentActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        sCurrentActivityWeakRef = new WeakReference<>(activity);
    }

    public String getBaseActivityName() {
        try {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null || currentActivity.isFinishing() || currentActivity.isDestroyed()) {
                return "";
            }
            ActivityManager activityManager = (ActivityManager) currentActivity.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return "";
            }
            List<RunningTaskInfo> groundActivity = activityManager.getRunningTasks(1);
            if (groundActivity == null || groundActivity.isEmpty()) {
                return "";
            }
            RunningTaskInfo sTaskInfo = groundActivity.get(0);
            if (sTaskInfo == null || sTaskInfo.baseActivity == null) {
                return "";
            }
            return sTaskInfo.baseActivity.getClassName();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
