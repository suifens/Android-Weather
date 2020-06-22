package com.goodtech.tq.helpers;

import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
        if (TextUtils.isEmpty(bdLocation.getCity())) {
            cityMode.cid = 0;
        } else {
            cityMode.listNum = 0;
            cityMode.cid = 1000;
            cityMode.lat = bdLocation.getLatitude();
            cityMode.lon = bdLocation.getLongitude();
            cityMode.city = String.format("%s %s", bdLocation.getDistrict(), bdLocation.getStreet());
        }
        Gson gson = new Gson();
        String json = gson.toJson(cityMode);
        SpUtils.getInstance().putString(Constants.SP_LOCATION, json);
    }

    /**
     * 获取当前定位
     */
    public static CityMode getLocation() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<CityMode>(){}.getType());
        } else {
            CityMode cityMode = new CityMode();
            cityMode.cid = 0;
            return cityMode;
        }
    }

    /**
     * 获取城市列表
     */
    public static ArrayList<CityMode> getLocationList() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION_LIST, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            ArrayList<CityMode> locations = gson.fromJson(json, new TypeToken<ArrayList<CityMode>>(){}.getType());
            if (locations == null) {
                locations = new ArrayList<>();
            }
            CityMode location = getLocation();
            if (location != null) {
                locations.add(0, getLocation());
            }
            return locations;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 添加城市
     */
    public static void addCity(CityMode city) {
        List<CityMode> locations = getLocationList();

        city.listNum = locations.size();
        locations.add(city);
        Gson gson = new Gson();
        String json = gson.toJson(locations);
        SpUtils.getInstance().putString(Constants.SP_LOCATION_LIST, json);
    }

}
