package com.goodtech.tq.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.goodtech.tq.R;
import com.goodtech.tq.modules.citySearch.CityHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.jpush.JPushHelper;
import com.goodtech.tq.models.CityCodeMode;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.JuheAlarmModel;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by loongggdroid on 2016/3/21.
 */
public class JAlarmReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        
        long intervalMillis = intent.getLongExtra("intervalMillis", 0);
        Log.e("BroadcastReceiver", "onReceive: intervalMillis= " + System.currentTimeMillis());

        JPushHelper.buildLocalNotification(context.getApplicationContext(),
                context.getString(R.string.app_name), "收到消息");

        new Thread(() -> {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = context;
            mHandler.sendMessage(msg);
        }).start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if (msg.obj instanceof Context) {
                        fetchAlarm((Context) msg.obj);
                    }
                    break;
            }
        }
    };

    //  获取天气预警
    public static void fetchAlarm(Context context) {
        if (context == null) {
            return;
        }
        
        if (!SpUtils.getInstance().getBoolean(Constants.REMINDER_WEATHER, true)) {
            //  不添加提醒
            return;
        }

        CityMode cityMode = LocationSpHelper.getLocation();
        if (cityMode == null || TextUtils.isEmpty(cityMode.getCity())) {
            return;
        }

        String province_code = null;
        String city_code = null;
        if (!TextUtils.isEmpty(cityMode.getCityCode())) {
            city_code = cityMode.getCityCode();
        }
        ArrayList<CityCodeMode> list = CityHelper.getCityCodes(context);
        for (CityCodeMode cityCodeMode : list) {
            if (cityCodeMode.getCity_name().contains(cityMode.getCity())) {
                province_code = cityCodeMode.getProvince_code();
                if (TextUtils.isEmpty(city_code)) {
                    city_code = cityCodeMode.getCity_code();
                }
                break;
            }
        }

        if (TextUtils.isEmpty(province_code) || TextUtils.isEmpty(city_code)) {
            return;
        }

        JuHeHelper.getInstance().fetchJuheAlarm(province_code, city_code, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (success) {
                        if (jsonObject != null && !jsonObject.isNull("result")) {
                            JSONArray data = jsonObject.getJSONArray("result");
                            ArrayList<JuheAlarmModel> list = new Gson().fromJson(String.valueOf(data), new TypeToken<ArrayList<JuheAlarmModel>>() {}.getType());
                            checkAlarmModels(context, list);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void checkAlarmModels(Context context, ArrayList<JuheAlarmModel> list) {
        if (context == null) {
            return;
        }
        
        CityMode cityMode = LocationSpHelper.getLocation();
        if (list != null && list.size() > 0) {
            JuheAlarmModel alarmModel = null;
            for (JuheAlarmModel temp : list) {
                if (cityMode == null) break;
                if (cityMode.getMergerName().contains(temp.getDistrict())) {
                    alarmModel = temp;
                    break;
                }
            }
            if (alarmModel == null) {
                alarmModel = list.get(0);
            }
            if (alarmModel != null) {
                //  建立推送
                String curDay = TimeUtils.timeToDay(System.currentTimeMillis());
                if (SpUtils.getInstance().getString("alarmDay", "") != curDay) {
                    SpUtils.getInstance().putString("alarmDay", TimeUtils.timeToDay(alarmModel.getTime(), "yyyy-MM-dd HH:mm"));
                    JPushHelper.buildLocalNotification(context.getApplicationContext(),
                            context.getString(R.string.app_name), alarmModel.getTitle());
                }
            }
        }
    }
}
