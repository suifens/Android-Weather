package com.goodtech.tq.httpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.goodtech.tq.app.App;
import com.goodtech.tq.modules.citySearch.CityHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.listener.CompletionListener;
import com.goodtech.tq.models.CityCodeMode;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.JuheAlarmModel;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.modules.others.airQuality.AirQualityHelper;
import com.goodtech.tq.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * com.goodtech.tq.httpClient
 */
public class WeatherHttpHelper {

    private static final String KEY_METADATA = "metadata";
    private static final String KEY_CONDITION = "conditionsshort";
    private static final String KEY_HOURLY = "fcsthourly24short";
    private static final String KEY_DAILY = "fcstdaily10short";
    private static final String KEY_FORECASTS = "forecasts";
    private static final String KEY_OBSERVATION = "observation";

    private static final String WEATHER_API = "%s/v1/geocode/%s/%s/aggregate.json?language=zh-CN&apiKey=e45ff1b7c7bda231216c7ab7c33509b8&products=conditionsshort,fcstdaily10short,fcsthourly24short,nowlinks";

    private final Context mContext;

    private String mBaseUrl = "https://api.weather.com";

    @SuppressLint("StaticFieldLeak")
    private static WeatherHttpHelper instance;

    public static synchronized WeatherHttpHelper getInstance() {
        if (instance == null) {
            instance = new WeatherHttpHelper(App.instance);
        }
        return instance;
    }

    public WeatherHttpHelper(Context context) {
        this.mContext = context;
    }

    public void getBaseUrl(CompletionListener callback) {
        if (!TextUtils.isEmpty(mBaseUrl)) {
            if (callback != null) {
                callback.onCompletion();
            }
            return;
        }
        String url = "https://www.yiguxm.com/weather_conf.json";
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (success) {
                        if (!jsonObject.isNull("api")) {
                            mBaseUrl = jsonObject.getString("api");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(mBaseUrl)) {
                    mBaseUrl = "https://api.weather.com";
                }
                if (callback != null) {
                    callback.onCompletion();
                }
            }
        });
    }

    public void fetchCitiesWeather() {
        ArrayList<CityMode> cityModes = LocationSpHelper.getCityListAndLocation();
        for (CityMode cityMode : cityModes) {
            fetchWeather(cityMode);
        }
    }

    public void fetchWeather(final CityMode cityMode) {
        fetchWeather(cityMode, null);
    }

    public boolean fetchWeather(final CityMode cityMode, final ApiCallback callback) {

        if (cityMode == null) {
            if (callback != null) {
                callback.onResponse(false, null, null);
            }
            return false;
        }

        Log.e("TAG", "fetchWeather:  ------------" + cityMode.getMergerName());
        if (!TextUtils.isEmpty(cityMode.getLat()) && !TextUtils.isEmpty(cityMode.getLon())) {
            long current = System.currentTimeMillis();
            long lastUpdate = WeatherSpHelper.getLastUpdate(cityMode.getPoiId());

            boolean needUpdate = current - lastUpdate > 5 * 60 * 1000;
            if (!needUpdate) {
                String curHour = TimeUtils.longToString(current, "HH");
                String lastHour = TimeUtils.longToString(lastUpdate, "HH");
                if (!curHour.equals(lastHour)) {
                    needUpdate = true;
                }
            }

            if (!needUpdate) {
                // 5 分钟内且同一小时：直接用缓存回调，避免重复网络请求
                WeatherModel cachedModel = WeatherSpHelper.getWeatherModel(cityMode.getPoiId());
                if (callback != null) {
                    callback.onResponse(true, cachedModel, null);
                }
                return true;
            }

            getWeather(cityMode, callback);
            return true;
        }
        return false;
    }

    public void getWeather(final CityMode cityMode, final ApiCallback callback) {
        if (cityMode == null || cityMode.getLat() == null || cityMode.getLon() == null) {
            if (callback != null) {
                callback.onResponse(false, null, null);
            }
            return;
        }

        //  获取aqi
        AirQualityHelper.fetchAqi(cityMode);

        AirQualityHelper.fetchCityLife(cityMode, null);
        //  获取提醒
        getAlarm(cityMode);

        ApiClient client = ApiClient.getInstance();
        String url = String.format(WEATHER_API, mBaseUrl, cityMode.getLat(), cityMode.getLon());

        if (url.isEmpty()) {
            if (callback != null) {
                callback.onResponse(false, null, null);
            }
            return;
        }

        client.get(url, null, new ApiResponseHandler(mContext) {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                if (success) {
                    WeatherSpHelper.saveWeather(jsonObject, cityMode.getPoiId());
                    WeatherModel model = parseWeatherJson(jsonObject, cityMode.getPoiId());

                    if (callback != null) {
                        callback.onResponse(true, model, errCode);
                    }
                } else {
                    if (callback != null) {
                        callback.onResponse(false, null, errCode);
                    }
                }
            }
        });
    }

    public static WeatherModel parseWeatherJson(JSONObject jsonObject, String poiId) {

        if (jsonObject == null) {
            return null;
        }

        WeatherModel model = new WeatherModel();
        model.poiId = poiId;

        //  24小时
        JsonObject hourlyElement = new Gson().fromJson(jsonObject.optString(KEY_HOURLY), JsonObject.class);
        if (hourlyElement != null) {
            JsonArray forecasts = new Gson().fromJson(hourlyElement.get(KEY_FORECASTS), JsonArray.class);

            model.hourlies = new Gson().fromJson(forecasts, new TypeToken<List<Hourly>>() {
            }.getType());
        }

        //  10天
        JsonObject dailyElement = new Gson().fromJson(jsonObject.optString(KEY_DAILY), JsonObject.class);
        if (dailyElement != null) {
            JsonArray forecasts = new Gson().fromJson(dailyElement.get(KEY_FORECASTS), JsonArray.class);

            model.dailies = new Gson().fromJson(forecasts, new TypeToken<List<Daily>>() {
            }.getType());

            String todayStr = TimeUtils.longToString(System.currentTimeMillis(), "MM月dd日");
            String yesterdayStr = TimeUtils.getYesterday();
            boolean needAddDay = true;
            for (Daily forecast : model.dailies) {
                String time = forecast.getTime();
                if (time.equals(yesterdayStr)) {
                    needAddDay = false;
                }
                if (time.equals(todayStr)) {
                    WeatherSpHelper.saveCurrentDay(new Gson().toJson(forecast), poiId);
                    break;
                }
            }
            if (needAddDay) {
                String yesterdayWeather = WeatherSpHelper.getYesterdayWeather(poiId);
                if (yesterdayWeather != null && !TextUtils.isEmpty(yesterdayWeather)) {
                    Daily daily = new Gson().fromJson(yesterdayWeather, new TypeToken<Daily>() {
                    }.getType());
                    if (daily != null) {
                        model.dailies.add(0, daily);
                    }
                }
            }
        }

        //
        JsonObject conditionElement = new Gson().fromJson(jsonObject.optString(KEY_CONDITION), JsonObject.class);
        if (conditionElement != null) {
            model.observation = new Gson().fromJson(conditionElement.get(KEY_OBSERVATION), Observation.class);
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

    public void getAlarm(final CityMode cityMode) {
        if (cityMode == null || TextUtils.isEmpty(cityMode.getCity())) {
            return;
        }

        String province_code = null;
        String city_code = null;
        if (!TextUtils.isEmpty(cityMode.getCityCode())) {
            city_code = cityMode.getCityCode();
        }
        ArrayList<CityCodeMode> list = CityHelper.getCityCodes(mContext);
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
                        if (!jsonObject.isNull("result")) {
                            JSONArray data = jsonObject.getJSONArray("result");
                            ArrayList<JuheAlarmModel> list = new Gson().fromJson(String.valueOf(data), new TypeToken<ArrayList<JuheAlarmModel>>() {}.getType());
                            if (list != null && list.size() > 0) {
                                WeatherSpHelper.saveAlarm(cityMode.getPoiId(), String.valueOf(data));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
