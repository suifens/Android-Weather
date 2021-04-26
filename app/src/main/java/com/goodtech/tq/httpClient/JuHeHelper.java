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

    //  新闻头条
    public final static String JUHE_TOUTIAO = "http://v.juhe.cn/toutiao/index?key=927352c7b578ec641b4bac8799b5d40b";
    //  运势
    public final static String JUHE_FORTUNE = "http://web.juhe.cn:8080/constellation/getAll?consName=%s&type=%s&key=a31e488d8a2cf98b0ad401625bbe7892";

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
        String url = JUHE_TOUTIAO;
        if (!TextUtils.isEmpty(type)) {
            url = url + String.format("&type=%s", type);
        }
        client.get(url, null, handler);
    }

    public void fetchFortune(String consName, String type, ApiResponseHandler handler) {
        String url = String.format(JUHE_FORTUNE, consName, type);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

}
