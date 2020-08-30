package com.goodtech.tq.helpers;

import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * com.goodtech.tq.helpers
 */
public class LocationSpHelper {

    /**
     * 保存当前定位
     */
    public static void saveWithLocation(BDLocation bdLocation) {
        CityMode cityMode = new CityMode();
        cityMode.location = true;
        if (TextUtils.isEmpty(bdLocation.getCity())) {
            if (getLocation().cid != 0) {
                return;
            }
            cityMode.cid = 0;
        } else {
            cityMode.listNum = 0;
            cityMode.cid = 1000;
            cityMode.lat = String.valueOf(bdLocation.getLatitude());
            cityMode.lon = String.valueOf(bdLocation.getLongitude());
            cityMode.city = String.format("%s %s", bdLocation.getDistrict(), bdLocation.getStreet());
            //  获取天气信息
            WeatherHttpHelper httpHelper = new WeatherHttpHelper(WeatherApp.getInstance());
            httpHelper.fetchWeather(cityMode);
        }
        Gson gson = new Gson();
        String json = gson.toJson(cityMode);
        SpUtils.getInstance().putString(Constants.SP_LOCATION, json);

        EventBus.getDefault().post(new MessageEvent().setLocation(cityMode.cid != 0));
    }

    /**
     * 获取当前定位
     */
    public static CityMode getLocation() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            CityMode mode = gson.fromJson(json, new TypeToken<CityMode>(){}.getType());
            if (mode != null) {
                return mode;
            }
        }

        CityMode cityMode = new CityMode();
        cityMode.location = true;
        cityMode.cid = 0;
        return cityMode;
    }

    public static void setCityList(ArrayList<CityMode> cityList) {
        ArrayList<CityMode> tempList = new ArrayList<>(cityList);
        for (int i = 0; i < cityList.size(); i++) {
            CityMode cityMode = cityList.get(i);
            if (cityMode.location) {
                tempList.remove(cityMode);
            }
        }

        for (int i = 0; i < tempList.size(); i++) {
            CityMode cityMode = tempList.get(i);
            cityMode.listNum = i + 1;
        }
        Gson gson = new Gson();
        String json = gson.toJson(tempList);
        SpUtils.getInstance().putString(Constants.SP_LOCATION_LIST, json);
    }

    /**
     * 获取定位和城市列表，定位index = 0
     */
    public static ArrayList<CityMode> getCityListAndLocation() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION_LIST, "");

        ArrayList<CityMode> locations = new ArrayList<>();
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            locations = gson.fromJson(json, new TypeToken<ArrayList<CityMode>>(){}.getType());
            if (locations == null) {
                locations = new ArrayList<>();
            }
        }

        CityMode location = getLocation();
        if (location != null) {
            locations.add(0, location);
        }
        return locations;
    }

    /**
     * 获取添加的城市列表
     */
    public static ArrayList<CityMode> getCityList() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION_LIST, "");
        ArrayList<CityMode> cityList = new ArrayList<>();
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            ArrayList<CityMode> locations = gson.fromJson(json, new TypeToken<ArrayList<CityMode>>() {
            }.getType());
            if (locations != null) {
                cityList = locations;
            }
        }
        return cityList;
    }

    /**
     * 添加城市
     */
    public static boolean addCity(CityMode city) {
        if (!canAddCity(city)) {
            return false;
        }
        List<CityMode> locations = getCityList();
        city.listNum = locations.size() + 1;
        locations.add(city);
        Gson gson = new Gson();
        String json = gson.toJson(locations);
        SpUtils.getInstance().putString(Constants.SP_LOCATION_LIST, json);
        return true;
    }

    private static boolean canAddCity(CityMode city) {
        List<CityMode> locations = getCityListAndLocation();
        for (CityMode cityMode : locations) {
            if (cityMode.cid == city.cid) {
                return false;
            }
        }
        return true;
    }

}
