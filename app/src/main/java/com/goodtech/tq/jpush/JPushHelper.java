package com.goodtech.tq.jpush;

import android.text.TextUtils;

import com.goodtech.tq.app.BaseApp;

import cn.jpush.android.api.JPushInterface;

public class JPushHelper {

    /**
     * 停止接收极光推送
     */
    public static void stopPush() {
        JPushInterface.stopPush(BaseApp.getInstance());
    }

    /**
     * 恢复极光推送
     */
    public static void resumePush() {
        if (!TextUtils.isEmpty(BaseApp.getInstance().getJPushRegId())) {


            JPushInterface.resumePush(BaseApp.getInstance());
        }
    }

}
