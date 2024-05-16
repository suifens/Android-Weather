package com.goodtech.tq.httpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.goodtech.tq.app.App;

/**
 * com.goodtech.tq.httpClient
 */
public class JuHeHelper {

    //  新闻头条
    public final static String JUHE_TOUTIAO = "http://v.juhe.cn/toutiao/index?key=927352c7b578ec641b4bac8799b5d40b";
    //  
    public final static String JUHE_FORTUNE = "http://web.juhe.cn/constellation/getAll?consName=%s&type=%s&key=a31e488d8a2cf98b0ad401625bbe7892";

    public final static String JUHE_DAY_DETAIL = "http://v.juhe.cn/calendar/day?date=%s&key=3fb26fed72acf7a7e4154d3a55f196c7";

    public final static String JUHE_MONTH_HOLIDAY = "http://v.juhe.cn/calendar/month?year-month=%s&key=3fb26fed72acf7a7e4154d3a55f196c7";
    //  城市空气质量
    public final static String JUHE_AIR_QUALITY = "http://web.juhe.cn/environment/air/cityair?city=%s&key=a23dce468957d0748adf7969a42ff219";
    //  城市空气PM2.5指数
    public final static String JUHE_AIR_PM = "http://web.juhe.cn/environment/air/pm?city=%s&key=a23dce468957d0748adf7969a42ff219";
    
    public final static String UPDATE_URL = "https://app.yiguxm.com/chunjing/yuzhi_banbenqingqiu.json";

    private Context mContext;

    @SuppressLint("StaticFieldLeak")
    private static JuHeHelper instance;

    public static synchronized JuHeHelper getInstance() {
        if (instance == null) {
            instance = new JuHeHelper(App.instance);
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

    /// 获取更新信息
    public void fetchNewVersion(ApiResponseHandler handler) {
        ApiClient client = ApiClient.getInstance();
        client.get(UPDATE_URL, null, handler);
    }

    public void fetchFortune(String consName, String type, ApiResponseHandler handler) {
        String url = String.format(JUHE_FORTUNE, consName, type);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

    public void fetchDayDetails(String day, ApiResponseHandler handler) {
        String url = String.format(JUHE_DAY_DETAIL, day);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

    public void fetchMonthHoliday(String yearMonth, ApiResponseHandler handler) {
        String url = String.format(JUHE_MONTH_HOLIDAY, yearMonth);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

    //  城市空气质量
    public void fetchAirQuality(String city, ApiResponseHandler handler) {
        String url = String.format(JUHE_AIR_QUALITY, city);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

    //  城市空气PM2.5指数
    public void fetchAirPM(String city, ApiResponseHandler handler) {
        String url = String.format(JUHE_AIR_PM, city);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

    //  根据城市查询天气
    public static final String JUHE_WEATHER = "http://apis.juhe.cn/simpleWeather/query?city=%s&key=9af0b9d910cf209a708ba81a2d694012";
    public void fetchJuheWeather(String city, ApiResponseHandler handler) {
        String url = String.format(JUHE_WEATHER, city);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

    //  根据城市查询生活指数
    public static final String JUHE_LIFE = "http://apis.juhe.cn/simpleWeather/life?city=%s&key=9af0b9d910cf209a708ba81a2d694012";
    public void fetchJuheLife(String city, ApiResponseHandler handler) {
        String url = String.format(JUHE_LIFE, city);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

    //  气象预警查询
    public static final String JUHE_ALARM = "https://apis.juhe.cn/fapig/alarm/queryV2?key=c6f53b5c5b62866f2f5b3a604a8fd81b&province_code=%s&city_code=%s";
    public void fetchJuheAlarm(String provinceCode, String cityCode, ApiResponseHandler handler) {
        String url = String.format(JUHE_ALARM, provinceCode, cityCode);
        ApiClient client = ApiClient.getInstance();
        client.get(url, null, handler);
    }

}
