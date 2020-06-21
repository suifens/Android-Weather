package com.goodtech.tq.citySearch;

import android.content.Context;

import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * com.goodtech.tq.citySearch
 */
public class CityHelper {

    public static ArrayList<CityMode> getRecommends(Context context) {
        ArrayList<CityMode> cityModes = new ArrayList<>();
        String cityJson = Utils.getJson(Constants.RECOMMEND_CITY, context);
        JsonObject jsonElement = new Gson().fromJson(cityJson, JsonObject.class);
        if (jsonElement != null) {
            JsonArray jsonArray = new Gson().fromJson(jsonElement.get("citys"), JsonArray.class);
            ArrayList<CityMode> list = new Gson().fromJson(jsonArray, new TypeToken<ArrayList<CityMode>>() {}.getType());
            cityModes.addAll(list);
        }
        if (cityModes.size() > 0) {
            CityMode location = LocationSpHelper.getLocation();
            if (location == null) {
                location = new CityMode();
                location.city = "定位失败";
            }
            cityModes.add(1, location);
        }
        return cityModes;
    }

}
