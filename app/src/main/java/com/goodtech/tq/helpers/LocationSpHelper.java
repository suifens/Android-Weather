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
        if (bdLocation.getTime() == null) {
            return;
        }
        CityMode cityMode = new CityMode();
        cityMode.listNum = 0;
        cityMode.cid = 1000;
        cityMode.lat = bdLocation.getLatitude();
        cityMode.lon = bdLocation.getLongitude();
        cityMode.city = String.format("%s %s", bdLocation.getDistrict(), bdLocation.getStreet());
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
            return null;
        }
    }

    /**
     * 获取城市列表
     */
    public static List<CityMode> getLocationList() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION_LIST, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            List<CityMode> locations = gson.fromJson(json, new TypeToken<List<CityMode>>(){}.getType());
            if (locations == null) {
                locations = new ArrayList<>();
            }
            CityMode location = getLocation();
            if (location != null && !TextUtils.isEmpty(location.city)) {
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
