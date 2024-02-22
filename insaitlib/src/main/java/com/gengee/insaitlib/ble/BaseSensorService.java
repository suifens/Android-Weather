package com.gengee.insaitlib.ble;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gengee.insaitlib.ble.core.BleConnection;
import com.gengee.insaitlib.ble.core.BleManagerCallbacks;
import com.gengee.insaitlib.ble.dic.BleDeviceType;
import com.gengee.insaitlib.ble.util.BleConst;
import com.gengee.insaitlib.ble.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@SuppressLint("MissingPermission")
public abstract class BaseSensorService extends Service {

    private static final String TAG = "BaseSensorService";
    private BluetoothDevice mDevice;

    protected Map<BluetoothDevice, BleConnection> mBleConnections;
    protected ArrayList<BluetoothDevice> mDevices;

    protected boolean mBound = false;

    public BaseSensorService() {
        super();
    }


    private BleManagerCallbacks mBleManagerCallbacks = new BleManagerCallbacks() {
        @Override
        public void onDeviceConnecting(BluetoothDevice device) {
            if (device != null) {
                LogUtil.e(TAG, device.getName() + " 设备开始连接中...");
            }
            sendConnectChange(device, BleConst.CONNECT_CONNECTING);
        }

        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            if (device != null) {
                LogUtil.e(TAG, device.getName() + " 设备连接成功！");
            }
            sendConnectChange(device, BleConst.CONNECT_CONNECTED);
        }

        @Override
        public void onDeviceReady(BluetoothDevice device) {
            if (device != null) {
                LogUtil.e(TAG, device.getName() + " onDeviceReady()");
            }
        }

        @Override
        public void onDeviceDisconnecting(BluetoothDevice device) {
            if (device != null) {
                LogUtil.e(TAG, device.getName() + " 设备断开连接中...");
            }
            sendConnectChange(device, BleConst.CONNECT_DISCONNECTING);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, boolean isUserDisconnect) {
            LogUtil.e(TAG,  device.getName() + " 设备已断开连接！isUserDisconnect=" + isUserDisconnect);
            if (device != null && !device.toString().equals("null")) {
                mBleConnections.remove(device);
                mDevices.remove(device);

                if (!isUserDisconnect) {
                    sendConnectChange(device, BleConst.CONNECT_DISCONNECTED);
                }
            }
        }

        @Override
        public void onLinklossOccur(BluetoothDevice device) {
            if (device != null) {
                LogUtil.e(TAG, device.getName() + " onLinklossOccur()");
            }
            sendConnectChange(device, BleConst.CONNECT_FAIL);
        }

        @Override
        public void onError(BluetoothDevice device, String message, int errorCode) {
//            if (isUpgrading) {
//                return;
//            }
            if (device != null) {
                LogUtil.e(TAG, device.getName() + " 连接错误，断开连接");
            }
            disconnectDevice(device);
        }

        protected void sendConnectChange(BluetoothDevice bluetoothDevice, int type) {
            final Intent intent = new Intent(BleConst.NOTIFICATION_CONNECT_CHANGE);
            intent.putExtra(BleConst.EXTRA_DEVICE, bluetoothDevice);
            intent.putExtra(BleConst.EXTRA_DATA, type);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    };

    public abstract BleDeviceType getDeviceType();


    public abstract class BaseSensorBinder extends Binder {

        /**
         * Disconnects from all connected devices
         */
        /*package access*/
        final void disconnectAllDevices() {

            if (mDevices != null) {
                for (BluetoothDevice device : mDevices) {
                    if (mBleConnections != null) {
                        BleConnection connection = mBleConnections.get(device);
                        if (connection != null) {
                            connection.doDisconnect(true);
                            mBleConnections.remove(device);
                        }
                    } else if (device.getBondState() != BluetoothDevice.BOND_NONE) {
                        BleConnection connection = new BleConnection(getBaseContext(), getDeviceType(), device, null);
                        connection.doDisconnect(true);
                    }
                }
                mDevices.clear();
            }

            if (mBleConnections != null && mBleConnections.size() > 0) {
                for (Map.Entry<BluetoothDevice, BleConnection> entry : mBleConnections.entrySet()) {
                    BleConnection connection = entry.getValue();
                    connection.doDisconnect(true);
                }
                mBleConnections.clear();
            }
        }

        final void disconnectDevice(BluetoothDevice device) {
            if (device == null) {
                return;
            }
            if (mBleConnections != null) {
                BleConnection connection = mBleConnections.get(device);
                if (connection != null) {
                    connection.doDisconnect(true);
                }
                mBleConnections.remove(device);
            }
            if (mDevices != null) {
                mDevices.remove(device);
            }
        }

        /**
         * Returns the list of connected devices
         */
        /*package access*/
        final List<BluetoothDevice> getConnectedDevices() {
            return Collections.unmodifiableList(mDevices);
        }

        /**
         * Returns the remote connection for the particualr bluetooth device.
         *
         * @param device bluetooth device
         */
        /*package access*/
        public abstract BleConnection getBleConnection(BluetoothDevice device); /*{
            return mThingyConnections.get(device);
        }*/

        /**
         * Selects the current bluetooth device.
         *
         * @param device bluetooth device
         */
        /*package access*/
        final void setSelectedDevice(final BluetoothDevice device) {
            mDevice = device;
        }

        /**
         * Returns the current bluetooth device which was selected from {@link #setSelectedDevice(BluetoothDevice)}.
         */
        /*package access*/
        final BluetoothDevice getSelectedDevice() {
            return mDevice;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBleConnections = new HashMap<>();
        mDevices = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BleConst.EXTRA_DEVICE);
            if (bluetoothDevice != null) {
                mBleConnections.put(bluetoothDevice, new BleConnection(this, getDeviceType(), bluetoothDevice, mBleManagerCallbacks));
                if (!mDevices.contains(bluetoothDevice)) {
                    mDevices.add(bluetoothDevice);
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy called on Base service");
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        disconnectFromAllDevices();
    }

    @Nullable
    @Override
    public abstract BaseSensorBinder onBind(Intent intent);

    @Override
    public final void onRebind(Intent intent) {
        mBound = true;
        onRebind();
    }

    @Override
    public final boolean onUnbind(Intent intent) {
        mBound = false;
        onUnbind();
        return true;
    }

    /**
     * Called when the activity has rebinded to the service after being recreated. This method is not called when the activity was killed and
     * recreated just to change the phone orientation.
     */
    protected void onRebind() {
    }

    /**
     * Called when the activity has unbound from the service after being bound.
     */
    protected void onUnbind() {

    }

    /**
     * Disconnects from all connected devices
     */
    final void disconnectFromAllDevices() {
        if (mBleConnections != null && mBleConnections.size() > 0) {
            for (Map.Entry<BluetoothDevice, BleConnection> entry : mBleConnections.entrySet()) {
                BleConnection connection = entry.getValue();
                connection.doDisconnect(true);
            }
            mBleConnections.clear();
        }

        if (mDevices != null) {
            mDevices.clear();
        }
    }

    final void disconnectDevice(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        if (mBleConnections != null) {
            BleConnection connection = mBleConnections.get(device);
            if (connection != null) {
                connection.doDisconnect(true);
            }
            mBleConnections.remove(device);
        }
        if (mDevices != null) {
            mDevices.remove(device);
        }
    }

    /**
     * Create your own notification target class to display notifications
     */
}
