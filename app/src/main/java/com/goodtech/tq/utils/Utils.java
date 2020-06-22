package com.goodtech.tq.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.TypedValue;

import com.goodtech.tq.R;
import com.goodtech.tq.httpClient.ErrorCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * com.goodtech.tq.utils
 *
 * @author: mac
 * @date: 2020/5/23
 */
public class Utils {

    /**
     * 获取ErrorCode类型
     * @param errorCodeStr
     * @return
     */
    public static ErrorCode resolveErrorCode(String errorCodeStr) {
        ErrorCode err= ErrorCode.UNKNOWN;
        try {
            err = ErrorCode.valueOf(errorCodeStr);
        } catch (IllegalArgumentException i) {
            //查询不到类型，采用默认ErrorCode.UNKNOWN
        }
        return err;
    }

    /**
     * 获取json文件字符串
     * @param fileName
     * @param context
     * @return
     */
    public static String getJson(String fileName, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static float dp2px(float dp) {
        Resources r = Resources.getSystem();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static int dp2pxInt(float dp) {
        return (int) dp2px(dp);
    }

}
