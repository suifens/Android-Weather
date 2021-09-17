package com.goodtech.tq.others.airQuality;

import android.text.TextUtils;
import android.util.Log;

import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.models.AirPmModel;
import com.goodtech.tq.models.AirQualityModel;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

/**
 * com.goodtech.tq.others.airQuality
 */
public class AirQualityManager {

    public interface AirPmListener {
        void onCompletion(AirPmModel pmModel);
    }

    /**
     * 获取城市空气PM2.5指数
     */
    public static void getAirPmData(String city, AirPmListener listener) {

        String result = SpUtils.getInstance().getString(String.format("pm_%s", city), "");
        if (result != null && !TextUtils.isEmpty(result)) {
            JsonObject resultJson = new Gson().fromJson(result, JsonObject.class);
            if (resultJson != null) {
                AirPmModel pmModel = new Gson().fromJson(resultJson, new TypeToken<AirPmModel>() {
                }.getType());
                if (pmModel != null) {
                    if (System.currentTimeMillis() - TimeUtils.switchTime(pmModel.getTime()) < 30 * 60 * 1000) {
                        if (listener != null) listener.onCompletion(pmModel);
                        return;
                    }
                }
            }
        }

        JuHeHelper.getInstance().fetchAirPM(city, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                Log.e("AirQualityManager", "onResponse: " + jsonObject.toString());
                AirPmModel pmModel = null;
                try {
                    if (success) {
                        JsonObject resultJson = new Gson().fromJson(jsonObject.optString("result"), JsonObject.class);
                        if (resultJson != null) {
                            SpUtils.getInstance().putString(String.format("pm_%s", city), jsonObject.optString("result"));
                            pmModel = new Gson().fromJson(resultJson, new TypeToken<AirPmModel>() {
                            }.getType());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listener != null) listener.onCompletion(pmModel);
            }
        });
    }

    public interface AirQualityListener {
        void onCompletion(AirQualityModel pmModel);
    }

    /**
     * 获取城市空气质量
     */
    public static void getAirQualityData(String city, AirQualityListener listener) {
        String result = SpUtils.getInstance().getString(String.format("quality_%s", city), "");
        if (result != null && !TextUtils.isEmpty(result)) {
            JsonObject resultJson = new Gson().fromJson(result, JsonObject.class);
            if (resultJson != null) {
                AirQualityModel qualityModel = new Gson().fromJson(resultJson, new TypeToken<AirQualityModel>() {
                }.getType());
                if (qualityModel != null && qualityModel.getCityNow() != null) {
                    if (System.currentTimeMillis() - TimeUtils.switchTime(qualityModel.getCityNow().getDate()) < 30 * 60 * 1000) {
                        if (listener != null) listener.onCompletion(qualityModel);
                        return;
                    }
                }
            }
        }

        JuHeHelper.getInstance().fetchAirPM(city, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                Log.e("AirQualityManager", "onResponse: " + jsonObject.toString());
                AirQualityModel qualityModel = null;
                try {
                    if (success) {
                        JsonObject resultJson = new Gson().fromJson(jsonObject.optString("result"), JsonObject.class);
                        if (resultJson != null) {
                            SpUtils.getInstance().putString(String.format("quality_%s", city), jsonObject.optString("result"));
                            qualityModel = new Gson().fromJson(resultJson, new TypeToken<AirQualityModel>() {
                            }.getType());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listener != null) listener.onCompletion(qualityModel);
            }
        });
    }

}

