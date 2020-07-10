package com.goodtech.tq.httpClient;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.SpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * com.goodtech.tq.httpClient
 */
public class WeatherHttpHelper {

    private static final String TAG = "WeatherHttpHelper";

    private static final String KEY_METADATA = "metadata";
    private static final String KEY_CONDITION = "conditionsshort";
    private static final String KEY_HOURLY = "fcsthourly24short";
    private static final String KEY_DAILY = "fcstdaily10short";
    private static final String KEY_FORECASTS = "forecasts";
    private static final String KEY_OBSERVATION = "observation";

    private static final String WEATHER_API = "https://api.weather.com/v1/geocode/%s/%s/aggregate.json?language=zh-CN&apiKey=e45ff1b7c7bda231216c7ab7c33509b8&products=conditionsshort,fcstdaily10short,fcsthourly24short,nowlinks";

    private Context mContext;

    public WeatherHttpHelper(Context context) {
        this.mContext = context;
    }

    public void fetchCitiesWeather() {
        ArrayList<CityMode> cityModes = LocationSpHelper.getCityListAndLocation();
        for (CityMode cityMode : cityModes) {
            fetchWeather(cityMode);
        }
    }

    public void fetchWeather(final CityMode cityMode) {
        if (cityMode != null && !TextUtils.isEmpty(cityMode.lat) && !TextUtils.isEmpty(cityMode.lon)) {

            JSONObject weatherJson = WeatherSpHelper.getWeatherJson(cityMode.cid);

            if (System.currentTimeMillis()/1000 > getExpireTime(weatherJson)) {
                getWeather(cityMode, null);
            }
        }
    }

    public void getWeather(final CityMode cityMode, final ApiCallback callback) {

        ApiClient client = ApiClient.getInstance();
        String url = String.format(WEATHER_API, cityMode.lat, cityMode.lon);

        client.get(url, null, new ApiResponseHandler(mContext) {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                if (success) {
                    WeatherSpHelper.saveWeather(jsonObject, cityMode.cid);
                    WeatherModel model = parseWeatherJson(jsonObject, cityMode.cid);
                    if (callback != null) {
                        callback.onResponse(true, model, errCode);
                    }
                } else {
                    if (callback != null) {
                        callback.onResponse(false, errCode);
                    }
                }
            }
        });
    }

    public static WeatherModel parseWeatherJson(JSONObject jsonObject, int cid) {

        if (jsonObject == null) {
            return null;
        }

        WeatherModel model = new WeatherModel();
        model.cid = cid;

        //  24小时
        JsonObject hourlyElement = new Gson().fromJson(jsonObject.optString(KEY_HOURLY), JsonObject.class);
        if (hourlyElement != null) {
            JsonArray forecasts = new Gson().fromJson(hourlyElement.get(KEY_FORECASTS), JsonArray.class);
            List<Hourly> hourlies = new Gson().fromJson(forecasts, new TypeToken<List<Hourly>>() {
            }.getType());

            model.hourlies = hourlies;
            Log.e(TAG, "onResponse: hourly = " + hourlies);
        }

        //  10天
        JsonObject dailyElement = new Gson().fromJson(jsonObject.optString(KEY_DAILY), JsonObject.class);
        if (dailyElement != null) {
            JsonArray forecasts = new Gson().fromJson(dailyElement.get(KEY_FORECASTS), JsonArray.class);
            List<Daily> dailies = new Gson().fromJson(forecasts, new TypeToken<List<Daily>>() {
            }.getType());

            model.dailies = dailies;
            Log.e(TAG, "onResponse: dailies = " + dailies);
        }

        //
        JsonObject conditionElement = new Gson().fromJson(jsonObject.optString(KEY_CONDITION), JsonObject.class);
        if (conditionElement != null) {
            Observation observation = new Gson().fromJson(conditionElement.get(KEY_OBSERVATION), Observation.class);
            model.observation = observation;
            Log.e(TAG, "onResponse: observation = " + observation);
        }

        //  定位和更新时间
        JsonObject metadata = new Gson().fromJson(jsonObject.optString(KEY_METADATA), JsonObject.class);
        if (metadata != null) {
            model.latitude = metadata.get("latitude").getAsDouble();
            model.longitude = metadata.get("longitude").getAsDouble();
            model.expireTime = metadata.get("expire_time_gmt").getAsLong();
        }

        return model;
    }

    public static long getExpireTime(JSONObject jsonObject) {

        if (jsonObject != null) {
            //  定位和更新时间
            JsonObject metadata = new Gson().fromJson(jsonObject.optString(KEY_METADATA), JsonObject.class);
            if (metadata != null) {
                return metadata.get("expire_time_gmt").getAsLong();
            }
        }

        return 0;
    }

}
