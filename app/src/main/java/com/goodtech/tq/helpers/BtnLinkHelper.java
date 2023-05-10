package com.goodtech.tq.helpers;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;
import com.goodtech.tq.httpClient.ApiClient;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.models.BtnLinkModel;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.utils.SpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * com.goodtech.tq.helpers
 */
public class BtnLinkHelper {
    public static void fetchBtnLinks() {
        ApiClient client = ApiClient.getInstance();
        String url = "https://app.yiguxm.com/pic/yztq_yhq.json";
        client.get(url, null, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                Log.e("TAG", "onResponse: ");

                try {
                    if (success) {
                        if (!jsonObject.isNull("startTime")) {
                            String startTime = jsonObject.getString("startTime");
                            long time = TimeUtils.string2Millis(startTime, "yyyy-MM-dd");
                            long lastTime = SpUtils.getInstance().getLong("btn_update_time", 0L);
                            if (time > lastTime) {
                                SpUtils.getInstance().putLong("btn_update_time", time);
                                if (!jsonObject.isNull("imgList")) {
                                    JSONArray list = jsonObject.getJSONArray("imgList");
                                    SpUtils.getInstance().putString("btn_links", String.valueOf(list));
                                } else {
                                    SpUtils.getInstance().putString("btn_links", "");
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    
    public static BtnLinkModel getBtnLink(String usingType) {
        String key = String.format("btn_link_%s", usingType);
        String linkStr = SpUtils.getInstance().getString(key, "");
        if (linkStr == null || linkStr.isEmpty()) {
            return null;
        }

        BtnLinkModel model = new Gson().fromJson(linkStr, new TypeToken<BtnLinkModel>(){ }.getType());
        return model;
    }

    public static BtnLinkModel getBtnLink(int index) {
        String linkStr = SpUtils.getInstance().getString("btn_links", "");
        List<BtnLinkModel> data = new Gson().fromJson(String.valueOf(linkStr), new TypeToken<List<BtnLinkModel>>() {
        }.getType());
        if (data != null && data.size() > index) {
            return data.get(index);
        }
        return null;
    }
}
