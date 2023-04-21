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
                            if (time > lastTime && !jsonObject.isNull("imgList")) {
                                JsonArray list = new Gson().fromJson((String) jsonObject.get("imgList"), JsonArray.class);
                                List<BtnLinkModel> data = new Gson().fromJson(list, new TypeToken<List<BtnLinkModel>>() {
                                }.getType());
                                if (data != null) {
                                    for (BtnLinkModel linkModel : data) {
                                        String key = String.format("btn_link_%s", linkModel.getUsingType());
                                        SpUtils.getInstance().putString(key, new Gson().toJson(linkModel));
                                    }
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
}
