package com.goodtech.tq.helpers;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.JuheAirModel;
import com.goodtech.tq.models.JuheAlarmModel;
import com.goodtech.tq.models.JuheLifeModel;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * com.goodtech.tq.helpers
 */
@SuppressLint("DefaultLocale")
public class WeatherSpHelper {
    public static void saveWeather(JSONObject jsonObject, int cid) {
        String key = String.format("weather_%d", cid);
        String timeKey = String.format("weather_%d_update", cid);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject.toString());
            SpUtils.getInstance().putLong(timeKey, System.currentTimeMillis());

            EventBus.getDefault().post(new MessageEvent().setFetchCId(cid));
        }
    }

    public static long getLastUpdate(int cid) {
        String timeKey = String.format("weather_%d_update", cid);
        return SpUtils.getInstance().getLong(timeKey, (long) 0);
    }

    /**
     * 获取当前定位
     */
    public static WeatherModel getWeatherModel(int cid) {

        JSONObject weatherJson = getWeatherJson(cid);

        WeatherModel weatherModel = WeatherHttpHelper.parseWeatherJson(weatherJson, cid);

        if (weatherModel != null && !TextUtils.isEmpty(getJuheAqi(cid))) {
            JuheAirModel airModel = new Gson().fromJson(getJuheAqi(cid), new TypeToken<JuheAirModel>(){ }.getType());
            if (airModel != null) {
                weatherModel.aqi = Integer.parseInt(airModel.getAqi());
            }
        }

        if (weatherModel != null && !TextUtils.isEmpty(getCityLife(cid))) {
            JuheLifeModel lifeModel = new Gson().fromJson(getCityLife(cid), new TypeToken<JuheLifeModel>(){ }.getType());
            if (lifeModel != null) {
                weatherModel.lifeModel = lifeModel;
            }
        }

        if (weatherModel != null && !TextUtils.isEmpty(getAlarm(cid))) {
            ArrayList<JuheAlarmModel> list = new Gson().fromJson(getAlarm(cid), new TypeToken<ArrayList<JuheAlarmModel>>() {}.getType());
            if (list != null && list.size() > 0) {
                weatherModel.alarmModel = list.get(0);
            }
        }

        return weatherModel;
    }

    public static JSONObject getWeatherJson(int cid) {
        String key = String.format("weather_%d", cid);
        String json = SpUtils.getInstance().getString(key, "");
        if (!TextUtils.isEmpty(json)) {
            try {
                return new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void saveJuheAqi(String jsonObject, int cid) {
        String key = String.format("aqi_%d", cid);
        String timeKey = String.format("aqi_%d_update", cid);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
            SpUtils.getInstance().putLong(timeKey, System.currentTimeMillis());
        }
    }

    public static String getJuheAqi(int cid) {
        String key = String.format("aqi_%d", cid);
        return SpUtils.getInstance().getString(key, "");
    }

    public static void saveCityLife(String jsonObject, int cid) {
        String key = String.format("life_%d", cid);
        String timeKey = String.format("life_%d_update", cid);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
            SpUtils.getInstance().putLong(timeKey, System.currentTimeMillis());

            EventBus.getDefault().post(new MessageEvent().setFetchCId(cid));
        }
    }

    public static String getCityLife(int cid) {
        String key = String.format("life_%d", cid);
        return SpUtils.getInstance().getString(key, "");
    }

    public static void deleteWeatherModel(int cid) {
        String key = String.format("weather_%d", cid);
        SpUtils.getInstance().remove(key);
    }

    public static void saveAlarm(int cid, String jsonObject) {
        String dayTime = TimeUtils.longToString(System.currentTimeMillis(), "MM-dd");
        String key = String.format("alarm_%d_%s", cid, dayTime);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
        }
    }

    public static String getAlarm(int cid) {
        String dayTime = TimeUtils.longToString(System.currentTimeMillis(), "MM-dd");
        String key = String.format("alarm_%d_%s", cid, dayTime);
        return SpUtils.getInstance().getString(key, "");
    }

    // <editor-fold defaultstate="collapsed" desc="昨天天气处理">
    public static void saveCurrentDay(String jsonObject, int cid) {
        String dayTime = TimeUtils.longToString(System.currentTimeMillis(), "MM-dd");
        String key = String.format("weather_%d_%s", cid, dayTime);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
        }
    }

    public static String getYesterdayWeather(int cid) {
        String key = String.format("weather_%d_%s", cid, TimeUtils.getYesterday());
        return SpUtils.getInstance().getString(key, "");
    }
    // </editor-fold>

}
