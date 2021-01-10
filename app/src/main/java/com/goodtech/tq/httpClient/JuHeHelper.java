package com.goodtech.tq.httpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.utils.Constants;

/**
 * com.goodtech.tq.httpClient
 */
public class JuHeHelper {

    private Context mContext;

    @SuppressLint("StaticFieldLeak")
    private static JuHeHelper instance;

    public static synchronized JuHeHelper getInstance() {
        if (instance == null) {
            instance = new JuHeHelper(WeatherApp.getInstance());
        }
        return instance;
    }

    public JuHeHelper(Context context) {
        this.mContext = context;
    }

    //  根据类型获取新闻
    public void fetchNews(String type, ApiResponseHandler handler) {
        ApiClient client = ApiClient.getInstance();
        String url = Constants.JUHE_TOUTIAO;
        if (!TextUtils.isEmpty(type)) {
            url = url + String.format("&type=%s", type);
        }
        client.get(url, null, handler);
    }

}
