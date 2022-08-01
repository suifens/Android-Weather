package com.goodtech.tq.location.helper;

import static com.goodtech.tq.app.BaseApp.FIRST_CHECK;

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

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.location.PoiRegion;
import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.location.services.LocationService;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.PermissionUtil;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.TipHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * com.goodtech.tq.location.service
 */
public class LocationHelper {

    private static final String TAG = "LocationSpHelper";
    private LocationService locationService;
    //  获取定位时间
    public static final String LOCATION_TIME = "LOCATION_TIME";

    private static class SingletonHolder {
        private static final LocationHelper INSTANCE = new LocationHelper();
    }
    private LocationHelper (){}
    public static final LocationHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private boolean isLocating = false;

    @SuppressLint("CheckResult")
    public void startWithDelay(final Activity context) {
        startWithDelay(context, false);
    }

    @SuppressLint("CheckResult")
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
        }
        else if (!TimeUtils.isCurrentDay(SpUtils.getInstance().getLong(Constants.TIME_LOCATION_CANCEL, 0L))
                || isForce) {

            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(granted ->
            {
                if (granted) {
                    BaseApp.getInstance().configLocation(context);
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
        TipHelper.showProgressDialog(context);
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(() -> {
            start(context);
            isLocating = true;
        }, 300);
    }

    private void start(Context context) {
        start(context, mListener);
    }

    private void start(Context context, BDAbstractLocationListener listener) {
        startTicker();
        if (!PermissionUtil.isLocationEnabled(context)) {
            removeTicker();
            return;
        }
        locationService = BaseApp.getInstance().locationService;
        if (locationService != null) {
            if (context.getClass() == Activity.class) {
                TipHelper.showProgressDialog((Activity) context);
            }
            locationService.registerListener(listener);
            LocationService.setLocationOption(locationService.getDefaultLocationClientOption());
            locationService.start();
        }
    }

    public void stop() {
        removeTicker();
        locationService = BaseApp.getInstance().locationService;
        // TipHelper.dismissProgressDialog();
        if (locationService != null) {
            locationService.unregisterListener(mListener); //注销掉监听
            locationService.stop(); //停止定位服务
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        /**
         * 定位请求回调函数
         *
         * @param location 定位结果
         */
        @Override
        public void onReceiveLocation(BDLocation location) {

            //保存
            LocationSpHelper.saveWithLocation(location);
            mHandler.postDelayed(() -> {
                LocationHelper.getInstance().stop();
            }, 300);

            if (location != null) {
                SpUtils.getInstance().putLong(Constants.TIME_LOCATION, System.currentTimeMillis());
            }

            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

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
                    sb.append(location.getLocType());
                    sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                    sb.append(location.getLocTypeDescription());
                    sb.append("\nlatitude : ");// 纬度
                    sb.append(location.getLatitude());
                    sb.append("\nlongtitude : ");// 经度
                    sb.append(location.getLongitude());
                    sb.append("\nradius : ");// 半径
                    sb.append(location.getRadius());
                    sb.append("\nCountryCode : ");// 国家码
                    sb.append(location.getCountryCode());
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
                    sb.append("\nTown : ");// 获取镇信息
                    sb.append(location.getTown());
                    sb.append("\nStreet : ");// 街道
                    sb.append(location.getStreet());
                    sb.append("\naddr : ");// 地址信息
                    sb.append(location.getAddrStr());
                    sb.append("\nStreetNumber : ");// 获取街道号码
                    sb.append(location.getStreetNumber());
                    sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                    sb.append(location.getUserIndoorState());
                    sb.append("\nDirection(not all devices have value): ");
                    sb.append(location.getDirection());// 方向
                    sb.append("\nlocationdescribe: ");
                    sb.append(location.getLocationDescribe());// 位置语义化信息
                    sb.append("\nPoi: ");// POI信息
                    if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                        for (int i = 0; i < location.getPoiList().size(); i++) {
                            Poi poi = (Poi) location.getPoiList().get(i);
                            sb.append("poiName:");
                            sb.append(poi.getName() + ", ");
                            sb.append("poiTag:");
                            sb.append(poi.getTags() + "\n");
                        }
                    }
                    if (location.getPoiRegion() != null) {
                        sb.append("PoiRegion: ");// 返回定位位置相对poi的位置关系，仅在开发者设置需要POI信息时才会返回，在网络不通或无法获取时有可能返回null
                        PoiRegion poiRegion = location.getPoiRegion();
                        sb.append("DerectionDesc:"); // 获取POIREGION的位置关系，ex:"内"
                        sb.append(poiRegion.getDerectionDesc() + "; ");
                        sb.append("Name:"); // 获取POIREGION的名字字符串
                        sb.append(poiRegion.getName() + "; ");
                        sb.append("Tags:"); // 获取POIREGION的类型
                        sb.append(poiRegion.getTags() + "; ");
                        sb.append("\nSDK版本: ");
                    }
                    sb.append(locationService.getSDKVersion()); // 获取SDK版本
                    if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                        sb.append("\nspeed : ");
                        sb.append(location.getSpeed());// 速度 单位：km/h
                        sb.append("\nsatellite : ");
                        sb.append(location.getSatelliteNumber());// 卫星数目
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 海拔高度 单位：米
                        sb.append("\ngps status : ");
                        sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                        sb.append("\ndescribe : ");
                        sb.append("gps定位成功");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                        // 运营商信息
                        if (location.hasAltitude()) {// *****如果有海拔高度*****
                            sb.append("\nheight : ");
                            sb.append(location.getAltitude());// 单位：米
                        }
                        sb.append("\noperationers : ");// 运营商信息
                        sb.append(location.getOperators());
                        sb.append("\ndescribe : ");
                        sb.append("网络定位成功");
                    } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                        sb.append("\ndescribe : ");
                        sb.append("离线定位成功，离线定位结果也是有效的");
                    } else if (location.getLocType() == BDLocation.TypeServerError) {
                        sb.append("\ndescribe : ");
                        sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                        sb.append("\ndescribe : ");
                        sb.append("网络不同导致定位失败，请检查网络是否通畅");
                    } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                        sb.append("\ndescribe : ");
                        sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                    }
                    Log.d(TAG, "onReceiveLocation: " + sb.toString());
                }
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            super.onConnectHotSpotMessage(s, i);
        }

        /**
         * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
         *
         * @param locType           当前定位类型
         * @param diagnosticType    诊断类型（1~9）
         * @param diagnosticMessage 具体的诊断信息释义
         */
        @Override
        public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
            super.onLocDiagnosticMessage(locType, diagnosticType, diagnosticMessage);
            int tag = 2;
            StringBuffer sb = new StringBuffer(256);
            sb.append("诊断结果: ");
            if (locType == BDLocation.TypeNetWorkLocation) {
                if (diagnosticType == 1) {
                    sb.append("网络定位成功，没有开启GPS，建议打开GPS会更好");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 2) {
                    sb.append("网络定位成功，没有开启Wi-Fi，建议打开Wi-Fi会更好");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeOffLineLocationFail) {
                if (diagnosticType == 3) {
                    sb.append("定位失败，请您检查您的网络状态");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeCriteriaException) {
                if (diagnosticType == 4) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 5) {
                    sb.append("定位失败，无法获取有效定位依据，请检查运营商网络或者Wi-Fi网络是否正常开启，尝试重新请求定位");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 6) {
                    sb.append("定位失败，无法获取有效定位依据，请尝试插入一张sim卡或打开Wi-Fi重试");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 7) {
                    sb.append("定位失败，飞行模式下无法获取有效定位依据，请关闭飞行模式重试");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 9) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeServerError) {
                if (diagnosticType == 8) {
                    sb.append("定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限");
                    sb.append("\n" + diagnosticMessage);
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
