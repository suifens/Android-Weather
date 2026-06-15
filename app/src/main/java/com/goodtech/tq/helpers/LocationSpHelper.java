package com.goodtech.tq.helpers;

import android.text.TextUtils;
import android.location.Address;
import android.location.Location;

import com.goodtech.tq.app.App;
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
import java.util.Objects;

/**
 * com.goodtech.tq.helpers
 */
public class LocationSpHelper {

    /** 定位城市占位 id，与 AirQualityActivity 等逻辑保持一致 */
    private static final int LOCATION_CID = 1000;
    private static final String LOCATION_POI_ID = "1000";

    /**
     * 保存当前定位
     */
    public static void saveWithLocation(Location location, Address address) {
        CityMode cityMode = new CityMode();
        cityMode.setLocation(true);
        String district = getDistrict(address);
        if (location == null || TextUtils.isEmpty(district)) {
            if (getLocation() != null) {
                EventBus.getDefault().post(new MessageEvent().setLocation(false));
            }
            return;
        } else {
            String cityName = getCityName(address, district);
            String poiName = getPoiName(address);
            String mergerName = buildMergerName(district, poiName);

            cityMode.setListNum(0);
            cityMode.setCid(LOCATION_CID);
            cityMode.setPoiId(LOCATION_POI_ID);
            cityMode.setLat(String.valueOf(location.getLatitude()));
            cityMode.setLon(String.valueOf(location.getLongitude()));
            cityMode.setCity(cityName);
            cityMode.setMergerName(mergerName);

            // 定位 cityCode 以 city.db 为准（010/021/0755...），不使用 Geocoder 邮编或 cityCode.json
            CityMode matched = DatabaseHelper.getInstance(App.instance)
                    .resolveLocationCity(cityName, district, poiName);
            if (matched != null) {
                if (!TextUtils.isEmpty(matched.getCityCode())) {
                    cityMode.setCityCode(matched.getCityCode());
                }
                if (!TextUtils.isEmpty(matched.getCity())) {
                    cityMode.setCity(matched.getCity());
                }
                // mergerName 保持「区县 + POI」展示格式，不用 city.db 全路径覆盖（对齐 Simple）
            }

            WeatherHttpHelper httpHelper = new WeatherHttpHelper(App.instance);
            httpHelper.getBaseUrl(() -> httpHelper.fetchWeather(cityMode));
        }
        Gson gson = new Gson();
        String json = gson.toJson(cityMode);
        SpUtils.getInstance().putString(Constants.SP_LOCATION, json);

        EventBus.getDefault().post(new MessageEvent().setLocation(cityMode.getCid() != 0));
    }

    private static String buildMergerName(String district, String poiName) {
        if (TextUtils.isEmpty(poiName)) {
            return district;
        }
        if (TextUtils.isEmpty(district)) {
            return poiName;
        }
        return String.format("%s %s", district, poiName);
    }

    private static String getDistrict(Address address) {
        if (address == null) {
            return "";
        }
        if (!TextUtils.isEmpty(address.getSubLocality())) {
            return address.getSubLocality();
        }
        if (!TextUtils.isEmpty(address.getLocality())) {
            return address.getLocality();
        }
        if (!TextUtils.isEmpty(address.getSubAdminArea())) {
            return address.getSubAdminArea();
        }
        if (!TextUtils.isEmpty(address.getAdminArea())) {
            return address.getAdminArea();
        }
        return "";
    }

    private static String getPoiName(Address address) {
        if (address == null) {
            return "";
        }
        if (!TextUtils.isEmpty(address.getFeatureName())) {
            return address.getFeatureName();
        }
        if (!TextUtils.isEmpty(address.getThoroughfare())) {
            return address.getThoroughfare();
        }
        return "";
    }

    private static String getCityName(Address address, String district) {
        if (address != null) {
            if (!TextUtils.isEmpty(address.getLocality())) {
                return address.getLocality();
            }
            if (!TextUtils.isEmpty(address.getSubAdminArea())) {
                return address.getSubAdminArea();
            }
            if (!TextUtils.isEmpty(address.getAdminArea())) {
                return address.getAdminArea();
            }
        }
        return district;
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
            if (cityMode != null && Objects.equals(cityMode.getPoiId(), city.getPoiId())) {
                return i;
            }
        }
        return -1;
    }

    private static boolean canAddCity(CityMode city) {
        List<CityMode> locations = getCityListAndLocation();
        for (CityMode cityMode : locations) {
            if (Objects.equals(cityMode.getPoiId(), city.getPoiId())) {
                return false;
            }
        }
        return true;
    }

}
