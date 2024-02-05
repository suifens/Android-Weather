package com.goodtech.tq.helpers;

import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.goodtech.tq.app.BaseApp;
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
    public static void saveWithLocation(AMapLocation location) {
        CityMode cityMode = new CityMode();
        cityMode.setLocation(true);
        if (location == null || TextUtils.isEmpty(location.getCity())) {
            if (getLocation() != null) {
                EventBus.getDefault().post(new MessageEvent().setLocation(false));
            }
            return;
        } else {
            cityMode.setListNum(0);
            cityMode.setCid(1000);
            cityMode.setLat(String.valueOf(location.getLatitude()));
            cityMode.setLon(String.valueOf(location.getLongitude()));
            cityMode.setCity(location.getCity());
            cityMode.setMergerName(String.format("%s %s", location.getDistrict(), location.getPoiName()));
            //  获取天气信息
            WeatherHttpHelper httpHelper = new WeatherHttpHelper(BaseApp.getInstance());
            httpHelper.getBaseUrl(() -> httpHelper.fetchWeather(cityMode));
        }
        Gson gson = new Gson();
        String json = gson.toJson(cityMode);
        SpUtils.getInstance().putString(Constants.SP_LOCATION, json);

        EventBus.getDefault().post(new MessageEvent().setLocation(cityMode.getCid() != 0));
    }

    /**
     * 获取当前定位
     */
    public static CityMode getLocation() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<CityMode>(){}.getType());
        }

        return null;
    }

    public static void setCityList(ArrayList<CityMode> cityList) {
        ArrayList<CityMode> tempList = new ArrayList<>(cityList);
        for (int i = 0; i < cityList.size(); i++) {
            CityMode cityMode = cityList.get(i);
            if (cityMode.getLocation()) {
                tempList.remove(cityMode);
            }
        }

//        for (int i = 0; i < tempList.size(); i++) {
//            CityMode cityMode = tempList.get(i);
//            cityMode.listNum = i + 1;
//        }
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
     * return 所在列表的位置
     */
    public static int addCity(CityMode city) {
        int index = getCityIndex(city);
        if (index == -1) {
            List<CityMode> locations = getCityList();
            locations.add(city);
            Gson gson = new Gson();
            String json = gson.toJson(locations);
            SpUtils.getInstance().putString(Constants.SP_LOCATION_LIST, json);
        }
        return index;
    }

    /**
     * 获取city所在列表的位置
     * @return 所在位置，不存在则返回-1
     */
    private static int getCityIndex(CityMode city) {
        List<CityMode> locations = getCityListAndLocation();
        for (int i = 0; i < locations.size(); i++) {
            CityMode cityMode = locations.get(i);
            if (cityMode != null && cityMode.getCid() == city.getCid()) {
                return i;
            }
        }
        return -1;
    }

    private static boolean canAddCity(CityMode city) {
        List<CityMode> locations = getCityListAndLocation();
        for (CityMode cityMode : locations) {
            if (cityMode.getCid() == city.getCid()) {
                return false;
            }
        }
        return true;
    }

}
