package com.goodtech.tq.httpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.models.VivoModel;
import com.goodtech.tq.utils.SpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * com.goodtech.tq.httpClient
 */
public class VivoHelper {

    private Context mContext;
    private VivoModel vivoModel;
    private static String VIVO_KEY = "vivo_key";

    @SuppressLint("StaticFieldLeak")
    private static VivoHelper instance;

    public static synchronized VivoHelper getInstance() {
        if (instance == null) {
            instance = new VivoHelper(BaseApp.getInstance());
        }
        return instance;
    }

    public VivoHelper(Context context) {
        this.mContext = context;
    }

    public String accessToken() {
        if (vivoModel == null) {
            vivoModel = getModel();
            if (vivoModel != null) {
                if (vivoModel.token_date > System.currentTimeMillis()) {
                    return vivoModel.access_token;
                } else {
                    refreshVivoToken(vivoModel.refresh_token);
                }
            } else {
                refreshVivoToken("a784d214f8a321f92fa98d922cbce5277bec879bd982ecdd890531612e8b6188");
            }
        }
        return null;
    }

    private VivoModel getModel() {
        JSONObject json = getModelJson();
        return new Gson().fromJson(String.valueOf(json), new TypeToken<VivoModel>() {
        }.getType());
    }

    private JSONObject getModelJson() {
        String json = SpUtils.getInstance().getString(VIVO_KEY, "");
        if (!TextUtils.isEmpty(json)) {
            try {
                return new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //  获取Access Token
    private void refreshVivoToken(String refresh_token) {
        String url = "http://marketing-api.vivo.com.cn/openapi/v1/oauth2/refreshToken?client_id=20220711022&client_secret=8DBE7042E3A0627DE6F5AD80903F441989D368F6E78C0E1B3F9240840F50A00F&refresh_token=" + refresh_token;
        ApiClient.getInstance().get(url, null, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (success && jsonObject != null && !jsonObject.isNull("data")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        VivoModel model = new Gson().fromJson(String.valueOf(data), new TypeToken<VivoModel>() {
                        }.getType());
                        if (model != null) {
                            SpUtils.getInstance().putString(VIVO_KEY, data.toString());
                            vivoModel = model;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    

}
