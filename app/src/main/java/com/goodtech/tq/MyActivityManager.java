package com.goodtech.tq;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.util.Log;

import com.goodtech.tq.app.WeatherApp;

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
        sCurrentActivityWeakRef = new WeakReference<Activity>(activity);
    }

    public String getBaseActivityName() {
        ActivityManager activityManager = (ActivityManager) WeatherApp.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        List groundActivity = activityManager.getRunningTasks(1);
        RunningTaskInfo sTaskInfo = (RunningTaskInfo) groundActivity.get(0);
        Log.e("TAG", "getBaseActivityName: " + sTaskInfo.baseActivity.getClassName());
        return sTaskInfo.baseActivity.getClassName();
    }
}
