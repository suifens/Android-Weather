package com.goodtech.tq.jpush;

import android.content.Context;
import android.text.TextUtils;

import com.goodtech.tq.app.BaseApp;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

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

    public static void buildLocalNotification(Context context, String tittle, String content){
        JPushLocalNotification ln = new JPushLocalNotification();
        ln.setBuilderId(0);
        ln.setContent(content);
        ln.setTitle(tittle);
        long id = System.currentTimeMillis()/1000;
        ln.setNotificationId(id) ;
        ln.setBroadcastTime(System.currentTimeMillis() + 1000 * 10);
        Map<String , Object> map = new HashMap<String, Object>() ;
        map.put("name", "jpush") ;
        map.put("test", "111") ;
        JSONObject json = new JSONObject(map) ;
        ln.setExtras(json.toString()) ;
        JPushInterface.addLocalNotification(context, ln);
    }

}
