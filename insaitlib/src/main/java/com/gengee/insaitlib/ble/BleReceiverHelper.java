package com.gengee.insaitlib.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gengee.insaitlib.ble.inter.BatteryStateListener;
import com.gengee.insaitlib.ble.inter.ConnectionListener;
import com.gengee.insaitlib.ble.inter.SensorDataListener;
import com.gengee.insaitlib.ble.util.BleConst;
import com.gengee.insaitlib.ble.util.LogUtil;

public class BleReceiverHelper {
    private static ThingyBroadcastReceiver mThingyBroadcastReceiver;

    protected final static String TAG = "BleReceiverHelper";

    /**
     * @param context  the application context
     * @param listener the listener to register
     */
    public static void registerGlobalConnectionListener(final Context context, final ConnectionListener listener) {

        if (mThingyBroadcastReceiver == null) {
            mThingyBroadcastReceiver = new ThingyBroadcastReceiver();

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BleConst.NOTIFICATION_ALGORITHM);
            intentFilter.addAction(BleConst.NOTIFICATION_ORIGINAL);
            intentFilter.addAction(BleConst.NOTIFICATION_BATTERY);
            intentFilter.addAction(BleConst.NOTIFICATION_DEVICE_STATE);
            intentFilter.addAction(BleConst.NOTIFICATION_CONNECT_CHANGE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_NOTIFICATION);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_WRITE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_READ);

            LocalBroadcastManager.getInstance(context).registerReceiver(mThingyBroadcastReceiver, intentFilter);
            LogUtil.d(TAG, "创建mThingyBroadcastReceiver");
        }
        mThingyBroadcastReceiver.setGlobalConnectionListener(listener);
    }

    /**
     * @param context  the application context
     * @param listener the listener to register
     */
    public static void registerConnectionListener(final Context context, final ConnectionListener listener, final BluetoothDevice device) {
        if (device == null) {
            LogUtil.e(TAG, "注册链接状态失败，device == null");
            return;
        }
        if (listener == null) {
            LogUtil.e(TAG, "注册链接状态失败，listener == null");
            return;
        }
        if (mThingyBroadcastReceiver == null) {
            mThingyBroadcastReceiver = new ThingyBroadcastReceiver();

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BleConst.NOTIFICATION_ALGORITHM);
            intentFilter.addAction(BleConst.NOTIFICATION_ORIGINAL);
            intentFilter.addAction(BleConst.NOTIFICATION_BATTERY);
            intentFilter.addAction(BleConst.NOTIFICATION_DEVICE_STATE);
            intentFilter.addAction(BleConst.NOTIFICATION_CONNECT_CHANGE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_NOTIFICATION);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_WRITE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_READ);

            LocalBroadcastManager.getInstance(context).registerReceiver(mThingyBroadcastReceiver, intentFilter);
            LogUtil.d(TAG, "创建mThingyBroadcastReceiver registerConnectionListener");
        }
        mThingyBroadcastReceiver.setConnectionListener(device, listener);
    }

    /**
     * Registers the {@link SensorDataListener}. Registered listener will receive the progress events from the BaseThingy service.
     *
     * @param context  the application context
     * @param listener the listener to register
     */
    public static void registerSensorDataListener(final Context context, final SensorDataListener listener, final BluetoothDevice device) {

        if (device == null || listener == null) {
            LogUtil.e(TAG, "registerSensorDataListener注册失败 listener=" + listener + " device" + device);
            return;
        }
        if (mThingyBroadcastReceiver == null) {
            mThingyBroadcastReceiver = new ThingyBroadcastReceiver();

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BleConst.NOTIFICATION_ALGORITHM);
            intentFilter.addAction(BleConst.NOTIFICATION_ORIGINAL);
            intentFilter.addAction(BleConst.NOTIFICATION_BATTERY);
            intentFilter.addAction(BleConst.NOTIFICATION_DEVICE_STATE);
            intentFilter.addAction(BleConst.NOTIFICATION_CONNECT_CHANGE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_NOTIFICATION);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_WRITE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_READ);

            LocalBroadcastManager.getInstance(context).registerReceiver(mThingyBroadcastReceiver, intentFilter);
            LogUtil.d(TAG, "创建mThingyBroadcastReceiver");
        }
        mThingyBroadcastReceiver.setSensorDataListener(device, listener);
    }

    public static void registerBatteryStateListener(final Context context, final BatteryStateListener listener, final BluetoothDevice device) {
        if (device == null || listener == null) {
            LogUtil.d(TAG, "registerBatteryStateListener注册失败 listener=" + listener + " device" + device);
            return;
        }
        if (mThingyBroadcastReceiver == null) {
            mThingyBroadcastReceiver = new ThingyBroadcastReceiver();

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BleConst.NOTIFICATION_ALGORITHM);
            intentFilter.addAction(BleConst.NOTIFICATION_ORIGINAL);
            intentFilter.addAction(BleConst.NOTIFICATION_BATTERY);
            intentFilter.addAction(BleConst.NOTIFICATION_DEVICE_STATE);
            intentFilter.addAction(BleConst.NOTIFICATION_CONNECT_CHANGE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_NOTIFICATION);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_WRITE);
            intentFilter.addAction(BleConst.NOTIFICATION_NORMAL_READ);

            LocalBroadcastManager.getInstance(context).registerReceiver(mThingyBroadcastReceiver, intentFilter);
            LogUtil.d(TAG, "创建mThingyBroadcastReceiver");
        }
        mThingyBroadcastReceiver.setBatteryStateListener(device, listener);
    }

    public static void unregisterSensorDataListener(final SensorDataListener listener) {
        if (mThingyBroadcastReceiver != null) {
            synchronized (mThingyBroadcastReceiver.mListenerLock) {
                boolean empty = mThingyBroadcastReceiver.removeAllListener(listener);
                Log.e(TAG, "unregisterListener: " + empty);
//                mThingyBroadcastReceiver.removeAllSensorListener();

            }
        }
    }

    public static void unregisterConnectListener(final ConnectionListener listener) {
        if (mThingyBroadcastReceiver != null) {
            synchronized (mThingyBroadcastReceiver.mListenerLock) {
                boolean empty = mThingyBroadcastReceiver.removeAllConnectListener(listener);
                Log.e(TAG, "unregisterConnectListener: " + empty);
            }
        }
    }

    public static void unregisterBatteryListener(final BatteryStateListener listener) {
        if (listener == null) {
            return;
        }
        if (mThingyBroadcastReceiver != null) {
            synchronized (mThingyBroadcastReceiver.mListenerLock) {
                boolean empty = mThingyBroadcastReceiver.removeAllBatteryListener(listener);
                Log.e(TAG, "unregisterBatteryListener: " + empty);
//                mThingyBroadcastReceiver.removeAllBatteryListener();
            }
        }
    }
}
