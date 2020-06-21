package com.goodtech.tq.app;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

//import com.baidu.mapapi.CoordType;
//import com.baidu.mapapi.SDKInitializer;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.location.services.LocationService;
import com.umeng.commonsdk.UMConfigure;

public class WeatherApp extends Application {
    public LocationService locationService;
    public Vibrator mVibrator;
    public LocationHelper locationHelper = new LocationHelper();
    private static WeatherApp mApplication;

    public static WeatherApp getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
//        SDKInitializer.initialize(getApplicationContext());
//        SDKInitializer.setCoordType(CoordType.BD09LL);

        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE, "");

        DatabaseHelper.getInstance(getApplicationContext()).openDatabase();
    }
}
