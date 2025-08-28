package com.chunjing.tq.jpush.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.chunjing.tq.ui.activity.MainActivity;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class PushMessageReceiver extends JPushMessageReceiver {

    private static final String TAG = "MyJPushReceiver";

    /**
     * 收到自定义消息回调
     * @param message 自定义消息
     */
    @Override
    public void onMessage(Context context, CustomMessage message) {
        Log.e(TAG, "[onMessage]:" + message);
//        // RxBusUtils.get().post(KEY_PUSH_MESSAGE, PushMessage.wrap(MessageType.TYPE_CUSTOM, message));
        if (!TextUtils.isEmpty(message.message)) {
//            JReceiveEntity entity = new Gson().fromJson(message.message, new TypeToken<JReceiveEntity>() {}.getType());
//            if (entity != null) {
//                EventBus.getDefault().post(entity);
//            }
        }
    }


    /**
     * 收到通知回调
     * @param message 通知消息
     */
    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage message) {
        Log.e(TAG, "[onNotifyMessageArrived]:" + message);
//        // RxBusUtils.get().post(KEY_PUSH_MESSAGE, PushMessage.wrap(MessageType.TYPE_NOTIFICATION, message));
    }

    /**
     * 点击通知回调
     *
     * @param context
     * @param message 通知消息
     */
    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
        Log.e(TAG, "[onNotifyMessageOpened]:" + message);
        try{
            //打开自定义的Activity
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            context.startActivity(i);
        } catch (Throwable ignored){

        }
    }


    /**
     * 清除通知回调
     *
     * 说明:
     * 1.同时删除多条通知，可能不会多次触发清除通知的回调
     * 2.只有用户手动清除才有回调，调接口清除不会有回调
     * @param message 通知消息
     */
    @Override
    public void onNotifyMessageDismiss(Context context, NotificationMessage message) {
        Log.e(TAG, "[onNotifyMessageDismiss]:" + message);

    }


    @Override
    public void onMultiActionClicked(Context context, Intent intent) {
        Log.e(TAG, "[onMultiActionClicked] 用户点击了通知栏上的按钮:" + intent.getExtras().getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA));
    }


    //======================下面的都是操作的回调=========================================//

    @Override
    public void onRegister(Context context, String registrationId) {
        Log.e(TAG, "[onRegister]:" + registrationId);
        // RxBusUtils.get().post(KEY_PUSH_EVENT, new PushEvent(EventType.TYPE_REGISTER, true, registrationId));
    }

    /**
     * 连接状态发生变化
     *
     * @param context
     * @param isConnected 是否已连接
     */
    @Override
    public void onConnected(Context context, boolean isConnected) {
        Log.e(TAG, "[onConnected]:" + isConnected);
        // RxBusUtils.get().post(KEY_PUSH_EVENT, new PushEvent(EventType.TYPE_CONNECT_STATUS_CHANGED, isConnected));
    }

    @Override
    public void onCommandResult(Context context, CmdMessage cmdMessage) {
        //注册失败+三方厂商注册回调
        Log.e(TAG,"[onCommandResult] "+cmdMessage);
        //cmd为10000时说明为厂商token回调
        if(cmdMessage!=null&&cmdMessage.cmd==10000&&cmdMessage.extra!=null){
            String token = cmdMessage.extra.getString("token");
            int platform = cmdMessage.extra.getInt("platform");
            String deviceName = "unkown";
            switch (platform){
                case 1:
                    deviceName = "小米";
                    break;
                case 2:
                    deviceName = "华为";
                    break;
                case 3:
                    deviceName = "魅族";
                    break;
                case 4:
                    deviceName = "OPPO";
                    break;
                case 5:
                    deviceName = "VIVO";
                    break;
                case 6:
                    deviceName = "ASUS";
                    break;
                case 8:
                    deviceName = "FCM";
                    break;
            }
            Log.e(TAG,"获取到 "+deviceName+" 的token:"+token);
        }
    }

    /**
     * 所有和标签相关操作结果
     *
     * @param context
     * @param jPushMessage
     */
    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        Log.e(TAG, "[onTagOperatorResult]:" + jPushMessage);
//        pushEvent pushEvent = new PushEvent(jPushMessage.getSequence(), jPushMessage.getErrorCode() == 0)
//                .setData(JPushInterface.getStringTags(jPushMessage.getTags()));
        // RxBusUtils.get().post(KEY_PUSH_EVENT, pushEvent);
    }


    /**
     * 所有和别名相关操作结果
     *
     * @param context
     * @param jPushMessage
     */
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        Log.e(TAG, "[onAliasOperatorResult]:" + jPushMessage);
//        PushEvent pushEvent = new PushEvent(jPushMessage.getSequence(), jPushMessage.getErrorCode() == 0)
//                .setData(jPushMessage.getAlias());
        // RxBusUtils.get().post(KEY_PUSH_EVENT, pushEvent);
    }

    /**
     * 标签状态检测结果
     *
     * @param context
     * @param jPushMessage
     */
    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        Log.e(TAG, "[onCheckTagOperatorResult]:" + jPushMessage);
//        PushEvent pushEvent = new PushEvent(jPushMessage.getSequence(), jPushMessage.getErrorCode() == 0)
//                .setData(jPushMessage);
        // RxBusUtils.get().post(KEY_PUSH_EVENT, pushEvent);
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        Log.e(TAG, "[onMobileNumberOperatorResult]:" + jPushMessage);
    }

    @Override
    public void onNotificationSettingsCheck(Context context, boolean isOn, int source) {
        super.onNotificationSettingsCheck(context, isOn, source);
        Log.e(TAG, "[onNotificationSettingsCheck] isOn:" + isOn + ",source:" + source);
    }
}
