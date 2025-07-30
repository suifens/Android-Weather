package com.goodtech.tq.helpers;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.JuheAirModel;
import com.goodtech.tq.models.JuheAlarmModel;
import com.goodtech.tq.models.LifeEntity;
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
    
    private static final String WEATHER_KEY_FORMAT = "weather_%s";
    private static final String WEATHER_UPDATE_FORMAT = "weather_%s_update";
    
    public static void saveWeather(JSONObject jsonObject, String poiId) {
        String key = String.format(WEATHER_KEY_FORMAT, poiId);
        String timeKey = String.format(WEATHER_UPDATE_FORMAT, poiId);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject.toString());
            SpUtils.getInstance().putLong(timeKey, System.currentTimeMillis());

            EventBus.getDefault().post(new MessageEvent().setFetchCId(poiId));
        }
    }

    public static long getLastUpdate(String poiId) {
        String timeKey = String.format(WEATHER_UPDATE_FORMAT, poiId);
        return SpUtils.getInstance().getLong(timeKey, (long) 0);
    }

    /**
     * 获取当前定位
     */
    public static WeatherModel getWeatherModel(String poiId) {
        if (poiId == null || poiId.isEmpty()) {
            return null;
        }

        JSONObject weatherJson = getWeatherJson(poiId);

        WeatherModel weatherModel = WeatherHttpHelper.parseWeatherJson(weatherJson, poiId);

        if (weatherModel != null && !TextUtils.isEmpty(getJuheAqi(poiId))) {
            JuheAirModel airModel = new Gson().fromJson(getJuheAqi(poiId), new TypeToken<JuheAirModel>(){ }.getType());
            if (airModel != null) {
                weatherModel.aqi = Integer.parseInt(airModel.getAqi());
            }
        }

        if (weatherModel != null && !TextUtils.isEmpty(getCityLife(poiId))) {
            LifeEntity lifeModel = new Gson().fromJson(getCityLife(poiId), new TypeToken<LifeEntity>(){ }.getType());
            if (lifeModel != null) {
                weatherModel.lifeModel = lifeModel;
            }
        }

        if (weatherModel != null && !TextUtils.isEmpty(getAlarm(poiId))) {
            ArrayList<JuheAlarmModel> list = new Gson().fromJson(getAlarm(poiId), new TypeToken<ArrayList<JuheAlarmModel>>() {}.getType());
            if (list != null && list.size() > 0) {
                weatherModel.alarmModel = list.get(0);
            }
        }

        return weatherModel;
    }

    public static JSONObject getWeatherJson(String poiId) {
        String key = String.format(WEATHER_KEY_FORMAT, poiId);
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

    public static void saveJuheAqi(String jsonObject, String poiId) {
        String key = String.format("aqi_%s", poiId);
        String timeKey = String.format("aqi_%s_update", poiId);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
            SpUtils.getInstance().putLong(timeKey, System.currentTimeMillis());
        }
    }

    public static String getJuheAqi(String poiId) {
        String key = String.format("aqi_%s", poiId);
        return SpUtils.getInstance().getString(key, "");
    }

    public static void saveCityLife(String jsonObject, String poiId) {
        String key = String.format("life_%s", poiId);
        String timeKey = String.format("life_%s_update", poiId);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
            SpUtils.getInstance().putLong(timeKey, System.currentTimeMillis());

            EventBus.getDefault().post(new MessageEvent().setFetchCId(poiId));
        }
    }

    public static String getCityLife(String poiId) {
        String key = String.format("life_%s", poiId);
        return SpUtils.getInstance().getString(key, "");
    }

    public static void deleteWeatherModel(String poiId) {
        String key = String.format(WEATHER_KEY_FORMAT, poiId);
        SpUtils.getInstance().remove(key);
    }

    public static void saveAlarm(String poiId, String jsonObject) {
        String dayTime = TimeUtils.longToString(System.currentTimeMillis(), "MM-dd");
        String key = String.format("alarm_%s_%s", poiId, dayTime);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
        }
    }

    public static String getAlarm(String poiId) {
        String dayTime = TimeUtils.longToString(System.currentTimeMillis(), "MM-dd");
        String key = String.format("alarm_%s_%s", poiId, dayTime);
        return SpUtils.getInstance().getString(key, "");
    }

    // <editor-fold defaultstate="collapsed" desc="昨天天气处理">
    public static void saveCurrentDay(String jsonObject, String poiId) {
        String dayTime = TimeUtils.longToString(System.currentTimeMillis(), "MM-dd");
        String key = String.format("weather_%s_%s", poiId, dayTime);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject);
        }
    }

    public static String getYesterdayWeather(String poiId) {
        String key = String.format("weather_%s_%s", poiId, TimeUtils.getYesterday());
        return SpUtils.getInstance().getString(key, "");
    }
    // </editor-fold>

}
