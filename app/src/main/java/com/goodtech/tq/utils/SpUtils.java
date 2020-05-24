package com.goodtech.tq.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.goodtech.tq.app.WeatherApp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * SharedPreferences工具类
 *
 * @author wangrengshun <wangrengshun@gengee.cn>
 */
public class SpUtils {
    private SharedPreferences sp;
    
    private static SpUtils instance;
    
    private SpUtils(Context context) {
        sp = context.getSharedPreferences("matches_sp", Context.MODE_PRIVATE);
    }
    
    public static synchronized SpUtils getInstance() {
        if (instance == null) {
            instance = new SpUtils(WeatherApp.getInstance());
        }
        return instance;
    }
    
    public SpUtils putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
        return this;
    }
    
    public int getInt(String key, int dValue) {
        return sp.getInt(key, dValue);
    }
    
    public SpUtils putLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
        return this;
    }
    
    public long getLong(String key, Long dValue) {
        return sp.getLong(key, dValue);
    }
    
    public SpUtils putFloat(String key, float value) {
        sp.edit().putFloat(key, value).apply();
        return this;
    }
    
    public Float getFloat(String key, Float dValue) {
        return sp.getFloat(key, dValue);
    }
    
    public SpUtils putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
        return this;
    }
    
    public Boolean getBoolean(String key, boolean dValue) {
        return sp.getBoolean(key, dValue);
    }
    
    public SpUtils putString(String key, String value) {
        sp.edit().putString(key, value).apply();
        return this;
    }
    
    public String getString(String key, String dValue) {
        return sp.getString(key, dValue);
    }

    //保存序列话对象
    public synchronized boolean saveSerializableObject(String key, Object object) {
        if (object instanceof Serializable) {
            SharedPreferences.Editor editor = sp.edit();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(object);//把对象写到流里
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                editor.putString(key, temp);
                return editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 使用案例
     * private TextView textView1;
     * textView1 = findView(R.id.textView1);
     *
     * @param key
     * @param <T>
     * @return
     */
    public synchronized <T> T getSerializableObject(String key) {

        String temp = sp.getString(key, "");
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        T o = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            o = (T) ois.readObject();
        } catch (IOException | ClassNotFoundException ignored) {

        }
        return o;
    }
    
    public void remove(String key) {
        if (isExist(key)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(key);
            editor.apply();
        }
    }
    
    public void clearAllData() {
        sp.edit().clear();
        sp.edit().apply();
    }
    
    public boolean isExist(String key) {
        return sp.contains(key);
    }
    
    public static boolean isExistValue(String key) {
        return !TextUtils.isEmpty(getInstance().getString(key, ""));
    }
    
    public void clearData() {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }
    
}
