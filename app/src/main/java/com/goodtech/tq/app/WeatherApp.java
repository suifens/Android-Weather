package com.goodtech.tq.app;

import android.app.Application;
import android.app.Service;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;

//import com.baidu.mapapi.CoordType;
//import com.baidu.mapapi.SDKInitializer;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.location.services.LocationService;
import com.umeng.commonsdk.UMConfigure;

public class WeatherApp extends Application {
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    public LocationService locationService;
    public Vibrator mVibrator;
    private static WeatherApp mApplication;

    public static WeatherApp getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        DatabaseHelper.getInstance(getApplicationContext()).openDatabase();
    }

    public void startUsingApp() {

        //  初始化定位sdk，建议在Application中创建
        locationService = new LocationService(getApplicationContext());

        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
//        SDKInitializer.initialize(getApplicationContext());
//        SDKInitializer.setCoordType(CoordType.BD09LL);

        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE, "");
    }
}
