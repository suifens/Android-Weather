package com.goodtech.tq.location.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.PermissionUtils;
import com.goodtech.tq.app.App;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.PermissionUtil;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.utils.Utils;
import com.goodtech.tq.views.MessageAlert;
import com.goodtech.tq.views.popup.TopTitlePopup;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * com.goodtech.tq.location.service
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";
    //  获取定位时间
    public static final String LOCATION_TIME = "LOCATION_TIME";

    private LostApiClient mLostApiClient = null;
    private LocationRequest mLocationRequest = null;

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
    private boolean isLocationRequesting = false;
    private final Object locationLock = new Object();

    private boolean tryAcquireLocationRequest() {
        synchronized (locationLock) {
            if (isLocationRequesting) {
                Log.d(TAG, "skip duplicated location request");
                return false;
            }
            isLocationRequesting = true;
            return true;
        }
    }

    private void releaseLocationRequest() {
        synchronized (locationLock) {
            isLocationRequesting = false;
        }
    }

    @SuppressLint("CheckResult")
    public void startWithDelay(final Context context) {
        startWithDelay(context, false);
    }

    @SuppressLint("CheckResult")
    /**
     * isForce 是否强制定位
     */
    public void startWithDelay(final Context context, boolean isForce) {
        if (!tryAcquireLocationRequest()) {
            return;
        }

        long locationTime = SpUtils.getInstance().getLong(Constants.TIME_LOCATION, 0L);

        if (PermissionUtils.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {

            if (System.currentTimeMillis() - locationTime > 5 * 60 * 1000 || isForce) {
                startLocation(context);
            } else {
                releaseLocationRequest();
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

            PermissionUtils.permission(Manifest.permission.ACCESS_FINE_LOCATION).callback(
                    (isAllGranted, granted, deniedForever, denied) -> {
                        popupView.dismiss();
                        if (isAllGranted) {
                            startLocation(context);
                        } else {
                            releaseLocationRequest();
                            if (isForce) {
                                Toast.makeText(context, "没有定位权限，无法获取您的位置", Toast.LENGTH_LONG).show();
                            }
                            //  取消定位权限判断的时间
                            SpUtils.getInstance().putLong(Constants.TIME_LOCATION_CANCEL, System.currentTimeMillis());
                        }
                    }
            ).request();
        } else {
            releaseLocationRequest();
            //  取消定位权限判断的时间
            // Toast.makeText(context, "没有定位权限，无法获取您的位置", Toast.LENGTH_LONG).show();
        }
    }

    private void startLocation(final Context context) {
        stop(false);
        mContext = context;
        if (context.getClass() == Activity.class) {
            TipHelper.showProgressDialog((Activity) context);
        }
        if (mLostApiClient == null) {
            configClient();
            return;
        }
        isLocating = start(context);
        if (!isLocating) {
            releaseLocationRequest();
        }
    }

    private void configClient() {
        try {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setFastestInterval(500);

            mLostApiClient = new LostApiClient.Builder(mContext)
                    .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected() {
                            isLocating = start(mContext);
                        }

                        @Override
                        public void onConnectionSuspended() {
                            removeTicker();
                            releaseLocationRequest();
                            TipHelper.dismissProgressDialog();
                            Log.e(TAG, "定位服务连接中断");
                        }
                    })
                    .build();
            mLostApiClient.connect();
        } catch (Exception e) {
            removeTicker();
            releaseLocationRequest();
            TipHelper.dismissProgressDialog();
            Log.e(TAG, "初始化定位客户端失败", e);
        }
    }

    @SuppressLint("MissingPermission")
    private Boolean start(Context context) {
        startTicker();
        if (!PermissionUtil.isLocationEnabled(context)) {
            removeTicker();
            releaseLocationRequest();
            TipHelper.dismissProgressDialog();
            if (Utils.isActivityAlive(context)) {
                (new Handler(Looper.getMainLooper())).post(() -> {
                    MessageAlert alert = new MessageAlert(context);
                    alert.setCancelable(true);
                    alert.setTitle("定位失败");
                    alert.setMessage("当前手机需要打开定位功能");
                    alert.setConfirmListener((dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        App.instance.startActivity(intent);
                    });
                    alert.show();
                });
            }
            return false;
        }
        if (mLostApiClient != null) {
            if (!mLostApiClient.isConnected()) {
                mLostApiClient.connect();
                return true;
            }
            if (context.getClass() == Activity.class) {
                TipHelper.showProgressDialog((Activity) context);
            }

            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mLostApiClient, mLocationRequest, mLocationListener
                );
                return true;
            } catch (Exception e) {
                removeTicker();
                releaseLocationRequest();
                TipHelper.dismissProgressDialog();
                Log.e(TAG, "请求定位失败", e);
                return false;
            }
        }
        releaseLocationRequest();
        return false;
    }

    public void stop() {
        stop(true);
    }

    private void stop(boolean shouldReleaseRequest) {
        removeTicker();
        if (shouldReleaseRequest) {
            releaseLocationRequest();
        }
        if (mLostApiClient != null) {
            try {
                if (mLostApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mLostApiClient, mLocationListener);
                    mLostApiClient.disconnect();
                }
            } catch (Exception e) {
                Log.e(TAG, "停止定位失败", e);
            }
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Address address = getAddressFromLocation(location);

            //保存
            LocationSpHelper.saveWithLocation(location, address);
            mHandler.postDelayed(() -> {
                LocationHelper.getInstance().stop();
            }, 300);

            if (location != null) {
                SpUtils.getInstance().putLong(Constants.TIME_LOCATION, System.currentTimeMillis());
            }
            TipHelper.dismissProgressDialog();
            releaseLocationRequest();

            // TODO Auto-generated method stub
            if (null != location) {

                {
                    StringBuffer sb = new StringBuffer(256);
                    sb.append("time : ");
                    /**
                     * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                     * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                     */
                    sb.append(location.getTime());
                    sb.append("\nlatitude : ");// 纬度
                    sb.append(location.getLatitude());
                    sb.append("\nlongtitude : ");// 经度
                    sb.append(location.getLongitude());
                    if (address != null) {
                        sb.append("\nProvince : ");
                        sb.append(address.getAdminArea());
                        sb.append("\ncity : ");
                        sb.append(address.getLocality());
                        sb.append("\nDistrict : ");
                        sb.append(address.getSubLocality());
                        sb.append("\nStreet : ");
                        sb.append(address.getThoroughfare());
                        sb.append("\naddr : ");
                        sb.append(address.getAddressLine(0));
                    }
                    Log.d(TAG, "onReceiveLocation: " + sb.toString());
                }
            }
        }
    };

    private Address getAddressFromLocation(Location location) {
        if (location == null || mContext == null) {
            return null;
        }
        try {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0);
            }
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, "逆地理编码失败", e);
        }
        return null;
    }

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
                stop();
            } else {
                mHandler.postAtTime(mCheckTicker, next);
            }
        }
    };

}
