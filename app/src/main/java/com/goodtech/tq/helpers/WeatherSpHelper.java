package com.goodtech.tq.helpers;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * com.goodtech.tq.helpers
 */
@SuppressLint("DefaultLocale")
public class WeatherSpHelper {

    /**
     * 保存当前定位
     */
//    @SuppressLint("DefaultLocale")
//    public static void saveWeather(WeatherModel weatherModel, int cid) {
//        String key = String.format("weather_%d", cid);
//        if (weatherModel != null) {
//            Gson gson = new Gson();
//            String json = gson.toJson(weatherModel);
//            SpUtils.getInstance().putString(key, json);
//        }
//    }

    public static void saveWeather(JSONObject jsonObject, int cid) {
        String key = String.format("weather_%d", cid);
        if (jsonObject != null) {
            SpUtils.getInstance().putString(key, jsonObject.toString());
        }
    }

    /**
     * 获取当前定位
     */
    public static WeatherModel getWeatherModel(int cid) {
        String key = String.format("weather_%d", cid);

        JSONObject weatherJson = getWeatherJson(cid);

        return WeatherHttpHelper.parseWeatherJson(weatherJson, cid);
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

    public static void deleteWeatherModel(int cid) {
        String key = String.format("weather_%d", cid);
        SpUtils.getInstance().remove(key);
    }

}
