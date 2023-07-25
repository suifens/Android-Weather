package com.goodtech.tq.app.config;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.bytedance.msdk.api.v2.GMAdConfig;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.GMPangleOption;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class GMAdManagerHolder {

    private static boolean sInit;

    public static void init(Context context) {
        doInit(context);
    }

    //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
    private static void doInit(@NonNull Context context) {
        if (!sInit) {
            GMMediationAdSdk.initialize(context, buildV2Config(context));
            sInit = true;
        }
    }

    public static GMAdConfig buildV2Config(Context context) {
        JSONObject jsonObject = null;
        //读取json文件，本地缓存的配置
        try {
            jsonObject = new JSONObject(getJson("androidlocalconfig.json",context));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new GMAdConfig.Builder()
                .setAppId(Constants.PGE_APP_ID)
                .setAppName(DeviceUtils.getAppName(BaseApp.getInstance()))
                .setDebug(true)//为了确保项目性能，建议上线前设置为false
                .setPangleOption(new GMPangleOption.Builder()
                        .setTitleBarTheme(GMAdConstant.TITLE_BAR_THEME_DARK)
                        .build())
                .build();
    }

    public static String getAndroidId(Context context) {
        String androidId = null;
        try {
            androidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidId;
    }

    public static String getJson(String fileName, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
