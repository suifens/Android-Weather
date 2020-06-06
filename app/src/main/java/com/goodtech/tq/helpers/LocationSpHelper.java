package com.goodtech.tq.helpers;

import com.baidu.location.BDLocation;
import com.goodtech.tq.location.Location;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;

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
        SpUtils.getInstance().saveSerializableObject(Constants.SP_LOCATION, location);
    }

    /**
     * 获取当前定位
     */
    public static Location getLocation() {
        return SpUtils.getInstance().getSerializableObject(Constants.SP_LOCATION);
    }

    /**
     * 获取城市列表
     */
    public static List<Location> getLocationList() {
        List<Location> locations = SpUtils.getInstance().getSerializableObject(Constants.SP_LOCATION_LIST);
        if (locations != null) {
            locations = new ArrayList<>();
        }
        locations.add(0, getLocation());
        return locations;
    }

    /**
     * 添加城市
     */
    public static void addCity(Location city) {
        List<Location> locations = SpUtils.getInstance().getSerializableObject(Constants.SP_LOCATION_LIST);
        if (locations != null) {
            locations = new ArrayList<>();
        }
        locations.add(city);
        SpUtils.getInstance().saveSerializableObject(Constants.SP_LOCATION, locations);
    }

}
