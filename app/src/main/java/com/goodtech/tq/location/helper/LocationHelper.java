package com.goodtech.tq.location.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.UtilsTransActivity;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.PermissionUtil;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.popup.TopTitlePopup;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

/**
 * com.goodtech.tq.location.service
 */
public class LocationHelper {

    private static final String TAG = "LocationSpHelper";
    //  获取定位时间
    public static final String LOCATION_TIME = "LOCATION_TIME";

    private AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    private AMapLocationClientOption mLocationOption = null;

    private Context mContext;

    private static class SingletonHolder {
        private static final LocationHelper INSTANCE = new LocationHelper();
    }

    private LocationHelper() {
    }

    public static final LocationHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private boolean isLocating = false;

    @SuppressLint("CheckResult")
    public void startWithDelay(final Activity context) {
        startWithDelay(context, false);
    }

    @SuppressLint("CheckResult")
    /**
     * isForce 是否强制定位
     */
    public void startWithDelay(final Activity context, boolean isForce) {

        if (isLocating) {
            return;
        }

        long locationTime = SpUtils.getInstance().getLong(Constants.TIME_LOCATION, 0L);

        RxPermissions rxPermissions = new RxPermissions(context);
        if (rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                || rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {

            if (System.currentTimeMillis() - locationTime > 5 * 60 * 1000 || isForce) {
                startLocation(context);
            }
        } else if (!TimeUtils.isCurrentDay(SpUtils.getInstance().getLong(Constants.TIME_LOCATION_CANCEL, 0L))
                || isForce) {

            BasePopupView popupView = new XPopup.Builder(context)
                    .isDestroyOnDismiss(true)
                    .popupPosition(PopupPosition.Top)
                    .hasShadowBg(false)
                    .hasStatusBarShadow(false)
                    .asCustom(new TopTitlePopup(context));
            popupView.show();

            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(granted ->
            {
                popupView.dismiss();
                if (granted) {
                    startLocation(context);
                } else {
                    if (isForce) {
                        Toast.makeText(context, "没有定位权限，无法获取您的位置", Toast.LENGTH_LONG).show();
                    }
                    //  取消定位权限判断的时间
                    SpUtils.getInstance().putLong(Constants.TIME_LOCATION_CANCEL, System.currentTimeMillis());
                }
            });
        } else {
            //  取消定位权限判断的时间
            // Toast.makeText(context, "没有定位权限，无法获取您的位置", Toast.LENGTH_LONG).show();
        }
    }

    private void startLocation(final Activity context) {
        stop();
        mContext = context;
        TipHelper.showProgressDialog(context);
        Handler mHandler = new Handler(Looper.getMainLooper());
        if (mLocationClient == null) {
            configClient();
            return;
        }
//        mHandler.post(() -> isLocating = start(context));
        isLocating = start(context);
    }

    private void configClient() {
        AMapLocationClient.updatePrivacyShow(mContext, true, true);
        AMapLocationClient.updatePrivacyAgree(mContext, true);
        try {
            mLocationClient = new AMapLocationClient(mContext);
            //设置定位回调监听
            mLocationClient.setLocationListener(mLocationListener);

            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);

            isLocating = start(mContext);
        } catch (Exception e) {

        }
    }

    private Boolean start(Context context) {
        startTicker();
        if (!PermissionUtil.isLocationEnabled(context)) {
            removeTicker();
            return false;
        }
        if (mLocationClient != null) {
            if (context.getClass() == Activity.class) {
                TipHelper.showProgressDialog((Activity) context);
            }

            //获取一次定位结果：
            //该方法默认为false。
            mLocationOption.setOnceLocation(true);

            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            //启动定位
            mLocationClient.startLocation();
            return true;
        }
        return false;
    }

    public void stop() {
        removeTicker();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {

            //保存
            LocationSpHelper.saveWithLocation(location);
            mHandler.postDelayed(() -> {
                LocationHelper.getInstance().stop();
            }, 300);

            if (location != null) {
                SpUtils.getInstance().putLong(Constants.TIME_LOCATION, System.currentTimeMillis());
            }

            // TODO Auto-generated method stub
            if (null != location) {

                {
                    int tag = 1;
                    StringBuffer sb = new StringBuffer(256);
                    sb.append("time : ");
                    /**
                     * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                     * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                     */
                    sb.append(location.getTime());
                    sb.append("\nlocType : ");// 定位类型
                    sb.append(location.getLocationType());
                    sb.append("\nlatitude : ");// 纬度
                    sb.append(location.getLatitude());
                    sb.append("\nlongtitude : ");// 经度
                    sb.append(location.getLongitude());
                    sb.append("\nProvince : ");// 获取省份
                    sb.append(location.getProvince());
                    sb.append("\nCountry : ");// 国家名称
                    sb.append(location.getCountry());
                    sb.append("\ncitycode : ");// 城市编码
                    sb.append(location.getCityCode());
                    sb.append("\ncity : ");// 城市
                    sb.append(location.getCity());
                    sb.append("\nDistrict : ");// 区
                    sb.append(location.getDistrict());
                    sb.append("\nStreet : ");// 街道
                    sb.append(location.getStreet());
                    sb.append("\naddr : ");// 地址信息
                    sb.append(location.getAddress());
                    sb.append("\nStreetNumber : ");// 获取街道号码
                    Log.d(TAG, "onReceiveLocation: " + sb.toString());
                }
            }
        }
    };

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    protected void removeTicker() {
        isLocating = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mHandler.hasCallbacks(mCheckTicker)) {
                mHandler.removeCallbacks(mCheckTicker);
            }
        } else {
            mHandler.removeCallbacks(mCheckTicker);
        }
    }

    protected void startTicker() {
        scanCount = 0;
        removeTicker();
        mHandler.post(mCheckTicker);
    }

    protected int scanCount = 0;
    protected final Runnable mCheckTicker = new Runnable() {
        public void run() {
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            if (scanCount++ > 10) {
                removeTicker();
            } else {
                mHandler.postAtTime(mCheckTicker, next);
            }
        }
    };

}
