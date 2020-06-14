package com.goodtech.tq.helpers;

import com.baidu.location.BDLocation;
import com.goodtech.tq.location.Location;
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
        Location location = Location.location(bdLocation);
        location.setListNum(0);
        location.setLocation(true);
        Gson gson = new Gson();
        String json = gson.toJson(location);
        SpUtils.getInstance().putString(Constants.SP_LOCATION, json);
    }

    /**
     * 获取当前定位
     */
    public static Location getLocation() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Location location = gson.fromJson(json, new TypeToken<Location>(){}.getType());
            return location;
        } else {
            return null;
        }
    }

    /**
     * 获取城市列表
     */
    public static List<Location> getLocationList() {
        String json = SpUtils.getInstance().getString(Constants.SP_LOCATION_LIST, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            List<Location> locations = gson.fromJson(json, new TypeToken<List<Location>>(){}.getType());
            if (locations != null) {
                locations = new ArrayList<>();
            }
            Location location = getLocation();
            if (location != null && !location.getAddrStr().isEmpty()) {
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
    public static void addCity(Location city) {
        List<Location> locations = getLocationList();

        city.setListNum(locations.size());
        locations.add(city);
        Gson gson = new Gson();
        String json = gson.toJson(locations);
        SpUtils.getInstance().putString(Constants.SP_LOCATION, json);
    }

}
