package com.goodtech.tq.modules.others.airQuality;

import android.text.TextUtils;

import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.JuheAirModel;
import com.goodtech.tq.models.JuheLifeModel;
import com.goodtech.tq.models.WeatherModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

/**
 * com.goodtech.tq.others.airQuality
 */
public class AirQualityHelper {

    /**
     * 通过聚合数据接口，获取天气aqi
     */
    public static void fetchAqi(final CityMode cityMode) {

        String city = cityMode.getCity();
        if (!TextUtils.isEmpty(city)) {
            if (city.endsWith("市")) {
                city = city.replace("市", "");
            }
            if (city.endsWith("县")) {
                city = city.replace("县", "");
            }
            if (city.endsWith("区")) {
                city = city.replace("区", "");
            }
        }

        JuHeHelper.getInstance().fetchJuheWeather(city, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (success) {
                        if (jsonObject != null && !jsonObject.isNull("result")) {
                            JSONObject data = jsonObject.getJSONObject("result").getJSONObject("realtime");
                            JuheAirModel model = new Gson().fromJson(String.valueOf(data), new TypeToken<JuheAirModel>(){ }.getType());
                            if (model != null) {
                                model.setUpdateTime(System.currentTimeMillis());
                                WeatherSpHelper.saveJuheAqi(new Gson().toJson(model), cityMode.getCid());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static interface CityLifeCallback {

        void onResponse(JuheLifeModel lifeModel);

    }
    /**
     * 通过聚合数据接口，获取天气aqi
     */
    public static void fetchCityLife(final CityMode cityMode, CityLifeCallback callback) {

        String city = cityMode.getCity();
        if (!TextUtils.isEmpty(city)) {
            if (city.endsWith("市")) {
                city = city.replace("市", "");
            }
            if (city.endsWith("县")) {
                city = city.replace("县", "");
            }
            if (city.endsWith("区")) {
                city = city.replace("区", "");
            }
        }

        JuHeHelper.getInstance().fetchJuheLife(city, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (success) {
                        if (jsonObject != null && !jsonObject.isNull("result")) {
                            JSONObject data = jsonObject.getJSONObject("result").getJSONObject("life");
                            JuheLifeModel model = new Gson().fromJson(String.valueOf(data), new TypeToken<JuheLifeModel>(){ }.getType());
                            if (model != null) {
                               WeatherSpHelper.saveCityLife(String.valueOf(data), cityMode.getCid());
                                if (callback != null) {
                                    callback.onResponse(model);
                                }
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (callback != null) {
                    callback.onResponse(null);
                }
            }
        });
    }

}
