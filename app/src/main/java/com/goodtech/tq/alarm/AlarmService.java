package com.goodtech.tq.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CityHelper;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * com.loonggg.alarmmanager.clock.alarm
 */
public class AlarmService extends Service {

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    fetchAlarm();
                    break;
            }
        }
    };

    //  获取天气预警
    private void fetchAlarm() {
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
        ArrayList<CityCodeMode> list = CityHelper.getCityCodes(AlarmService.this);
        for (CityCodeMode cityCodeMode : list) {
            if (cityCodeMode.getCity_name().contains(cityMode.getCity())) {
                province_code = cityCodeMode.getProvince_code();
                city_code = cityCodeMode.getCity_code();
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
                        if (!jsonObject.isNull("result")) {
                            JSONArray data = jsonObject.getJSONArray("result");
                            ArrayList<JuheAlarmModel> list = new Gson().fromJson(String.valueOf(data), new TypeToken<ArrayList<JuheAlarmModel>>() {}.getType());
                            checkAlarmModels(list);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkAlarmModels(ArrayList<JuheAlarmModel> list) {
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
                JPushHelper.buildLocalNotification(AlarmService.this.getApplicationContext(),
                        getString(R.string.app_name), alarmModel.getTitle());
            }
        }
    }

    /**
     * 每1分钟更新一次数据
     */
    private static final int ONE_Miniute=20*1000;
    private static final int PENDING_REQUEST=0;

    public AlarmService() {}

    /**
     * 调用Service都会执行到该方法
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (SpUtils.getInstance().getLong(Constants.KEY_CALENDAR, 0L) > System.currentTimeMillis()) {
            return super.onStartCommand(intent, flags, startId);
        }

        //这里模拟后台操作
//        new Thread(() -> mHandler.sendEmptyMessage(1)).start();

        long interval = getTimeInMillis() - System.currentTimeMillis();

        //通过AlarmManager定时启动广播
        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        long firstTime = SystemClock.elapsedRealtime();
        long triggerAtTime = firstTime + interval;//从开机到现在的毫秒书（手机睡眠(sleep)的时间也包括在内
        Intent i = new Intent(this, JAlarmReceiver.class);
//        Intent i = new Intent();
        i.setAction("alarm");
        i.putExtra("tmac", "tracy mcgrady");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, i, 0);

        Log.e("TAG", "onStartCommand: " + firstTime + "  tri = " + triggerAtTime );
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, 60 * 1000, pIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private long getTimeInMillis(){
        //得到日历实例，主要是为了下面的获取时间
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        //获取当前毫秒值
        long systemTime = System.currentTimeMillis();
        //是设置日历的时间，主要是让日历的年月日和当前同步
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        //设置在几点提醒 设置的为13点
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        //设置在几分提醒 设置的为25分
        mCalendar.set(Calendar.MINUTE, minute + 1);
        //下面这两个看字面意思也知道
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        // 上面设置的就是13点25分的时间点
        // 获取上面设置的13点25分的毫秒值
        long selectTime = mCalendar.getTimeInMillis(); // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
            SpUtils.getInstance().putLong(Constants.KEY_CALENDAR, mCalendar.getTimeInMillis());
        }
        return mCalendar.getTimeInMillis();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
