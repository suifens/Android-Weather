package com.goodtech.tq.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.mmkv.MMKV;
import com.goodtech.tq.app.BaseApp;


/**
 * SharedPreferences工具类
 *
 * @author wangrengshun <wangrengshun@gengee.cn>
 */
public class SpUtils {

    public static final String VERSION_APP = "version"; //应用版本号
    public static final String AGREE_PERMISSION = "agree_permission"; //同意权限
    private static final String TAG = "SpUtils";
    private MMKV preferences;

    private static SpUtils instance;

    private SpUtils(Context context) {
        preferences = MMKV.defaultMMKV();
    }

    public static synchronized SpUtils getInstance() {
        if (instance == null) {
            instance = new SpUtils(BaseApp.getInstance());
        }
        return instance;
    }

    public void updateWithUserId(String userId) {
        preferences = null;
        preferences = MMKV.mmkvWithID(userId);

        //  4.5.2中将 SharedPreferences 迁移到 MMKV 中
        if (DeviceUtils.getVersionCode(BaseApp.getInstance()) >= 151) {

            SharedPreferences old_man = BaseApp.getInstance().getSharedPreferences("matches_sp", Context.MODE_PRIVATE);
            if (old_man.getAll().size() > 0) {
                //  迁移旧数据
                preferences.importFromSharedPreferences(old_man);
                //  清除数据
                old_man.edit().clear().apply();
            }
        }
    }

    // <editor-fold defaulted="collapsed" desc="是否同意权限">
    public boolean isAgreePermission() {
        boolean isAgree = preferences.decodeBool(AGREE_PERMISSION, true);
        return isAgree;
    }

    public void setPermissionAgree(boolean isAgree) {
        preferences.putBoolean(AGREE_PERMISSION, isAgree);
    }
    // </editor-fold>

    public SpUtils putInt(String key, int value) {
        preferences.encode(key, value);
        return this;
    }

    public int getInt(String key, int dValue) {
        return preferences.decodeInt(key, dValue);
    }

    public SpUtils putLong(String key, long value) {
        preferences.encode(key, value);
        return this;
    }

    public long getLong(String key, Long dValue) {
        return preferences.decodeLong(key, dValue);
    }

    public SpUtils putFloat(String key, float value) {
        preferences.encode(key, value);
        return this;
    }

    public Float getFloat(String key, Float dValue) {
        return preferences.decodeFloat(key, dValue);
    }

    public SpUtils putBoolean(String key, boolean value) {
        preferences.encode(key, value);
        return this;
    }

    public Boolean getBoolean(String key, boolean dValue) {
        return preferences.decodeBool(key, dValue);
    }

    public SpUtils putString(String key, String value) {
        preferences.encode(key, value);
        return this;
    }

    public String getString(String key, String dValue) {
        return preferences.decodeString(key, dValue);
    }

    public void remove(String key) {
        preferences.removeValueForKey(key);
    }

//    public void clearAllData() {
//        sp.edit().clear();
//        sp.edit().apply();
//    }

    public static boolean isExistValue(String key) {
        return !TextUtils.isEmpty(getInstance().getString(key, ""));
    }



}
