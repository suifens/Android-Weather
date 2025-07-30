package com.gengee.insaitlib.ble.core;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gengee.insaitlib.ble.dic.BleDeviceType;
import com.gengee.insaitlib.ble.inter.ScanListener;
import com.gengee.insaitlib.ble.model.BleAdvertisedData;
import com.gengee.insaitlib.ble.util.BleConst;
import com.gengee.insaitlib.ble.util.BleUtil;
import com.gengee.insaitlib.ble.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 蓝牙设备扫描
 */
public class BLEScanner {

    protected List<String> PREFIX_BLE_LIST = new ArrayList<>();

    private static final String TAG = "BLEScanner";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter;

    private ScanListener mScanListener; // 扫描监听器
    private Context mContext;

    // api 21 之后使用的类
    private BluetoothLeScanner mLeScanner;
    private MyLeCallback mLeCallback;

    //扫描的设备类型
    protected BleDeviceType mDeviceType;

    @SuppressLint("NewApi")
    public BLEScanner(@NonNull Context context, @NonNull BleDeviceType deviceType) {
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBtAdapter = mBluetoothManager.getAdapter();
            if (mBtAdapter == null) {
                LogUtil.d(TAG, "Unable to obtain a BluetoothAdapter.");
            }
        } else {
            LogUtil.d(TAG, "Unable to initialize BluetoothManager.");
        }

        mContext = context;

        this.mDeviceType = deviceType;
        initData();

        mLeScanner = mBtAdapter.getBluetoothLeScanner();
        mLeCallback = new MyLeCallback();

    }

    protected void initData() {
        PREFIX_BLE_LIST.clear();
        switch (mDeviceType) {
            case FOOTBALL:
                PREFIX_BLE_LIST.add("G-INSAIT");
                PREFIX_BLE_LIST.add("G-4-INSAIT");
                PREFIX_BLE_LIST.add("G-5-INSAIT");
                PREFIX_BLE_LIST.add("G-SPTDC");
                break;
            case SHINGUARD:
                PREFIX_BLE_LIST.add(BleConst.PREFIX_SHIN_L);
                PREFIX_BLE_LIST.add(BleConst.PREFIX_SHIN_R);
                break;
            case PAN:
                PREFIX_BLE_LIST.add("INSAIT-");
            default:
                break;
        }
    }

    /**
     * 搜索蓝牙回调，使用 BluetoothLeScanner().startScan(new MyLeCallback()) 设置回调
     */
    @SuppressLint("NewApi")
    private class MyLeCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            dealWithScanResult(result.getDevice(), result.getRssi(), Objects.requireNonNull(result.getScanRecord()).getBytes());
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed: ");
//            if (isUserStop) {
//                return;
//            }
//            if (mBtAdapter != null) {
//                // 一旦发生错误，除了重启蓝牙再没有其它解决办法
//                mBtAdapter.disable();
//                new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        while (true) {
//                            try {
//                                Thread.sleep(5000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                                break;
//                            }
//                            //要等待蓝牙彻底关闭，然后再打开，才能实现重启效果
//                            if (mBtAdapter.getState() == BluetoothAdapter.STATE_OFF) {
//                                mBtAdapter.enable();
//                                break;
//                            }
//                        }
//                    }
//
//                }).start();
//            }
        }
    }

    /**
     * 注册扫描监听器
     */
    public void setScanListener(ScanListener listener) {
        LogUtil.d(TAG, "registerScanCallback");
        mScanListener = listener;
    }

    /**
     * 扫描蓝牙设备，这些设备广播参数中包含的的服务
     */
    public boolean startScan() {
        return startScan(null);
    }

    @SuppressLint("NewApi")
    public boolean startScan(List<ScanFilter> filters) {
        isUserStop = false;
        if (mBtAdapter == null) {
            mBtAdapter = mBluetoothManager.getAdapter();
            if (mBtAdapter == null) {
                LogUtil.d(TAG, "mBtAdapter == null! startScan fail");
                return false;
            }
        }
        mLeScanner = mBtAdapter.getBluetoothLeScanner();
        if (mLeScanner == null) {
            LogUtil.d(TAG, "mLeScanner == null! startScan fail");
            return false;
        }
        LogUtil.d(TAG, "is ble scan started successfully: ");
        ScanSettings builder = new ScanSettings.Builder()
                //设置高功耗模式
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        mLeScanner.startScan(filters, builder, mLeCallback);
        return true;
    }

    private boolean isUserStop;

    public void stopScanBle() {
        isUserStop = true;
        stopScan();
    }

    /**
     * 停止扫描蓝牙设备
     */
    public void stopScan() {
        if (mBtAdapter == null) {
            LogUtil.d(TAG, "mBtAdapter == null! stopScan fail");
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            LogUtil.d(TAG, "mBtAdapter is not turned ON ! stopScan fail");
            return;
        }
        if (mLeScanner == null) {
            LogUtil.d(TAG, "mLeScanner == null! stopScan fail");
            return;
        }
        mLeScanner.stopScan(mLeCallback);
    }

    // 扫描回调接口
    private BluetoothAdapter.LeScanCallback mLeScanCallback = this::dealWithScanResult;

    protected String mScanName;

    /**
     * 搜索回调后数据处理
     *
     * @param rssi 信号强弱
     */
    private void dealWithScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mScanName = device.getName();

        if (mScanName == null && scanRecord != null) {
            final BleAdvertisedData badata = BleUtil.parseAdertisedData(scanRecord);
            mScanName = badata.getName();
        }

        if (isValidSensor(mScanName)) {
            if (scanRecord != null) {
                // LogUtil.i(TAG,mScanName + " 广播包：" + BleUtil.bytesToHex(scanRecord, 0));
                if (mScanListener != null) {
                    mScanListener.onDeviceFound(mDeviceType, device, rssi, scanRecord);
                }
            }
        }
    }

    public boolean isValidSensor(String name) {
        if (TextUtils.isEmpty(name) || PREFIX_BLE_LIST.size() == 0) {
            return false;
        }
        boolean isValid = false;
        for (String prefixBle : PREFIX_BLE_LIST) {
            if (name.startsWith(prefixBle)) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

    /**
     * 判断是否支持蓝牙
     */
    public boolean isBleSupport() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 判断蓝牙是否打开
     */
    public boolean isBleAvailable() {
        return mBtAdapter.isEnabled();
    }

    /**
     * 打开蓝牙连接
     */
    public static void openBle(Context context) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(enableBtIntent);
    }

    public void destroy() {
        mContext = null;
    }
}
