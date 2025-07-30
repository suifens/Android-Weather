package com.gengee.insaitlib.ble;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gengee.insaitlib.ble.inter.BatteryStateListener;
import com.gengee.insaitlib.ble.inter.ConnectionListener;
import com.gengee.insaitlib.ble.inter.SensorDataListener;
import com.gengee.insaitlib.ble.model.BatteryInfo;
import com.gengee.insaitlib.ble.util.BleConst;
import com.gengee.insaitlib.ble.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThingyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ThingyBroadcastReceiver";

    private final Map<String, Set<SensorDataListener>> mSensorDataListeners = new HashMap<>();
    private final Map<String, Set<BatteryStateListener>> mBatteryStateListeners = new HashMap<>();
    private final Map<String, Set<ConnectionListener>> mConnectingListeners = new HashMap<>();
    private final List<ConnectionListener> mGlobalConnectionListeners = new ArrayList<>();

    protected final Object mListenerLock = new Object();

    protected ExecutorService mExecutorService = Executors.newCachedThreadPool();

    public void setGlobalConnectionListener(final ConnectionListener listener) {
        synchronized (mGlobalConnectionListeners) {
            if (listener != null) {
                mGlobalConnectionListeners.add(listener);
                LogUtil.d(TAG, "添加mGlobalConnectionListeners = " + mGlobalConnectionListeners.size());
            }
        }
    }

    public void setConnectionListener(final BluetoothDevice device, final ConnectionListener listener) {
        synchronized (mConnectingListeners) {
            if (device == null) {
                return;
            }
            Set<ConnectionListener> dataListenerSet = this.mConnectingListeners.get(device.getAddress());
            if (dataListenerSet == null) {
                dataListenerSet = new HashSet<>();
            }
            dataListenerSet.add(listener);
            this.mConnectingListeners.put(device.getAddress(), dataListenerSet);
        }
    }

    public void setSensorDataListener(final BluetoothDevice device, final SensorDataListener listener) {
        synchronized (mSensorDataListeners) {
            if (device == null) {
                return;
            }
            Set<SensorDataListener> dataListenerSet = this.mSensorDataListeners.get(device.getAddress());
            if (dataListenerSet == null) {
                dataListenerSet = new HashSet<>();
            }
            dataListenerSet.add(listener);
            this.mSensorDataListeners.put(device.getAddress(), dataListenerSet);
        }
    }

    public void setBatteryStateListener(final BluetoothDevice device, final BatteryStateListener listener) {
        synchronized (mListenerLock) {
            if (device == null) {
                return;
            }
            Set<BatteryStateListener> dataListenerSet = this.mBatteryStateListeners.get(device.getAddress());
            if (dataListenerSet == null) {
                dataListenerSet = new HashSet<>();
            }
            dataListenerSet.add(listener);
            this.mBatteryStateListeners.put(device.getAddress(), dataListenerSet);
        }
    }

    public void  removeAllSensorListener() {
        synchronized (mSensorDataListeners) {
            mSensorDataListeners.clear();
        }
    }
    public boolean removeAllListener(final SensorDataListener listener) {

        boolean isEmpty = true;
        synchronized (mSensorDataListeners) {
            // We do it 2 times as the listener was added for 2 addresses
            for (final Map.Entry<String, Set<SensorDataListener>> entry : mSensorDataListeners.entrySet()) {
                Set<SensorDataListener> dataListenerSet = entry.getValue();
                dataListenerSet.remove(listener);
                if (!dataListenerSet.isEmpty()) {
                    isEmpty = false;
                }
            }
        }

        return isEmpty;
    }

    public void removeAllConnectListener() {
        synchronized (mConnectingListeners) {
            mConnectingListeners.clear();
        }
    }

    public void removeAllGlobalConnectListener() {
        synchronized (mGlobalConnectionListeners) {
            mGlobalConnectionListeners.clear();
        }
    }

    public boolean removeAllConnectListener(final ConnectionListener listener) {
        synchronized (mGlobalConnectionListeners) {
            Iterator<ConnectionListener> iterator = mGlobalConnectionListeners.listIterator();
            while (iterator.hasNext()) {
                ConnectionListener connectionListener = iterator.next();
                if (connectionListener == listener) {
                    iterator.remove();
                }
            }
        }

        boolean isEmpty = true;
        synchronized (mConnectingListeners) {
            // We do it 2 times as the listener was added for 2 addresses
            for (final Map.Entry<String, Set<ConnectionListener>> entry : mConnectingListeners.entrySet()) {
                Iterator<ConnectionListener> setIt = entry.getValue().iterator();
                while (setIt.hasNext()) {
                    ConnectionListener connectionListener = setIt.next();
                    if (connectionListener == listener) {
                        setIt.remove();
                    }
                }
            }
            // for (final Map.Entry<String, Set<ConnectionListener>> entry : mConnectingListeners.entrySet()) {
            //     Set<ConnectionListener> connectionListenerSet = entry.getValue();
            //     connectionListenerSet.remove(listener);
            //     if (!connectionListenerSet.isEmpty()) {
            //         isEmpty = false;
            //     }
            // }
            LogUtil.e(TAG, "移除 mConnectingListeners = " + mConnectingListeners.size());
        }
//            return mGlobalConnectionListeners.isEmpty();
        return isEmpty;
    }

    public void  removeAllBatteryListener() {
        synchronized (mBatteryStateListeners) {
            mBatteryStateListeners.clear();
        }
    }

    public boolean removeAllBatteryListener(final BatteryStateListener listener) {

        boolean isEmpty = true;

        synchronized (mListenerLock) {
            for (Map.Entry<String, Set<BatteryStateListener>> entry : mBatteryStateListeners.entrySet()) {
                Set<BatteryStateListener> dataListenerSet = entry.getValue();
                dataListenerSet.remove(listener);
                if (!dataListenerSet.isEmpty()) {
                    isEmpty = false;
                }
            }
        }

        return isEmpty;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (mListenerLock) {
            final BluetoothDevice device = intent.getParcelableExtra(BleConst.EXTRA_DEVICE);
            final String action = intent.getAction();

            if (device == null) {
                LogUtil.e(TAG, "设备为空！ action = " + action);
                return;
            }
            final UUID uuid;
            final byte[] data;
            final boolean isSuccess;
            if (action == null) return;
            switch (action) {
                case BleConst.NOTIFICATION_NORMAL_READ:
                    LogUtil.d(TAG, "收到读取数据通知");
                    data = intent.getExtras().getByteArray(BleConst.EXTRA_DATA);
                    uuid = (UUID) intent.getExtras().getSerializable(BleConst.EXTRA_COMMAND_UUID);
                    isSuccess = intent.getExtras().getBoolean(BleConst.EXTRA_COMMAND_RESULT);

                    mExecutorService.execute(() -> {
                        synchronized (mSensorDataListeners) {
                            Set<SensorDataListener> dataListenerSet = mSensorDataListeners.get(device.getAddress());
                            if (dataListenerSet != null && dataListenerSet.size() > 0) {
                                for (final SensorDataListener sensorDataListener : dataListenerSet) {
                                    sensorDataListener.onReadCommandResult(uuid, data, isSuccess);
                                }
                            }
                        }
                    });
                    break;
                case BleConst.NOTIFICATION_NORMAL_WRITE:
                    data = intent.getExtras().getByteArray(BleConst.EXTRA_COMMAND_DATA);
                    uuid = (UUID) intent.getExtras().getSerializable(BleConst.EXTRA_COMMAND_UUID);
                    isSuccess = intent.getExtras().getBoolean(BleConst.EXTRA_COMMAND_RESULT);

                    mExecutorService.execute(() -> {
                        synchronized (mSensorDataListeners) {
                            Set<SensorDataListener> dataListenerSet = mSensorDataListeners.get(device.getAddress());
                            if (dataListenerSet != null && dataListenerSet.size() > 0) {
                                for (final SensorDataListener sensorDataListener : dataListenerSet) {
                                    sensorDataListener.onWriteCommandResult(uuid, data, isSuccess);
                                }
                            }
                        }
                    });
                    break;

                case BleConst.NOTIFICATION_NORMAL_NOTIFICATION:
                    data = intent.getExtras().getByteArray(BleConst.EXTRA_COMMAND_DATA);
                    uuid = (UUID) intent.getExtras().getSerializable(BleConst.EXTRA_COMMAND_UUID);
                    isSuccess = intent.getExtras().getBoolean(BleConst.EXTRA_COMMAND_RESULT);
//
                    mExecutorService.execute(() -> {
                        synchronized (mSensorDataListeners) {
                            Set<SensorDataListener> dataListenerSet = mSensorDataListeners.get(device.getAddress());
                            if (dataListenerSet != null && dataListenerSet.size() > 0) {
                                for (final SensorDataListener sensorDataListener : dataListenerSet) {
                                    sensorDataListener.onNotifyCommandResult(uuid, data, isSuccess);
                                }
                            }
                        }
                    });
                    break;
                case BleConst.NOTIFICATION_BATTERY://电池电量
                    LogUtil.d(TAG, "收到设备电池电量通知");
                    final BatteryInfo batteryInfo = (BatteryInfo) intent.getExtras().getParcelable(BleConst.EXTRA_DATA);

                    mExecutorService.execute(() -> {
                        synchronized (mBatteryStateListeners) {
                            Set<BatteryStateListener> batteryStateListenerSet = mBatteryStateListeners.get(device.getAddress());
                            if (batteryStateListenerSet != null && batteryStateListenerSet.size() > 0) {
                                for (final BatteryStateListener batteryStateListener : batteryStateListenerSet) {

                                    batteryStateListener.onBatteryStateChanged(batteryInfo);
                                }
                            }
                        }
                    });
                    break;
                case BleConst.NOTIFICATION_DEVICE_STATE://设备状态
                    LogUtil.d(TAG, "收到设备状态信息通知");
                    uuid = (UUID) intent.getExtras().getSerializable(BleConst.EXTRA_COMMAND_UUID);
                    final int value = intent.getIntExtra(BleConst.EXTRA_SENSOR_STATE, 0);
                    final byte state = SensorManager.getInstance().resolveState((byte)value, device);
                    final int type = intent.getIntExtra(BleConst.EXTRA_SENSOR_TYPE, 0);

                    mExecutorService.execute(() -> {
                        synchronized (mSensorDataListeners) {
                            Set<SensorDataListener> dataListenerSet = mSensorDataListeners.get(device.getAddress());
                            if (dataListenerSet != null && dataListenerSet.size() > 0) {
                                // 设备状态
                                for (final SensorDataListener sensorDataListener : dataListenerSet) {
                                    sensorDataListener.onDeviceStateChange(uuid, state, type);
                                }
                            }
                        }
                    });

                    break;
                case BleConst.NOTIFICATION_ALGORITHM://传感数据
                case BleConst.NOTIFICATION_ORIGINAL:
                    // Find proper listeners

                    data = intent.getExtras().getByteArray(BleConst.EXTRA_DATA);
                    mExecutorService.execute(() -> {
                        synchronized (mSensorDataListeners) {
                            Set<SensorDataListener> dataListenerSet = mSensorDataListeners.get(device.getAddress());
                            if (dataListenerSet != null && dataListenerSet.size() > 0) {
                                for (final SensorDataListener sensorDataListener : dataListenerSet) {
                                    sensorDataListener.onDataUpdated(data);
                                }
                            }
                        }
                    });

                    break;
                case BleConst.NOTIFICATION_CONNECT_CHANGE:
                    int connectType = intent.getIntExtra(BleConst.EXTRA_DATA, 0);
                    LogUtil.d(TAG, "收到连接状态变化通知 connectType=" + connectType + " size=" + mGlobalConnectionListeners.size());
                    switch (connectType) {
                        case BleConst.CONNECT_CONNECTING:

                            mExecutorService.execute(() -> {
                                synchronized (mGlobalConnectionListeners) {
                                    for (ConnectionListener connectionListener : mGlobalConnectionListeners) {
                                        connectionListener.onConnecting();

                                    }
                                }

                                synchronized (mConnectingListeners) {
                                    Set<ConnectionListener> connectionListeners = mConnectingListeners.get(device.getAddress());
                                    if (connectionListeners != null && connectionListeners.size() > 0) {
                                        for (final ConnectionListener connectionListener : connectionListeners) {
                                            connectionListener.onConnecting();
                                        }
                                    }
                                }
                            });


                            break;
                        case BleConst.CONNECT_CONNECTED:

                            mExecutorService.execute(() -> {
                                synchronized (mGlobalConnectionListeners) {
                                    for (ConnectionListener connectionListener : mGlobalConnectionListeners) {
                                        connectionListener.onServiceDiscovered();

                                    }
                                }

                                synchronized (mConnectingListeners) {
                                    Set<ConnectionListener> connectionListeners = mConnectingListeners.get(device.getAddress());
                                    if (connectionListeners != null && connectionListeners.size() > 0) {
                                        for (final ConnectionListener connectionListener : connectionListeners) {
                                            connectionListener.onServiceDiscovered();
                                        }
                                    }
                                }
                            });

                            break;
                        case BleConst.CONNECT_DISCONNECTED:
                            mExecutorService.execute(() -> {
                                synchronized (mGlobalConnectionListeners) {
                                    for (ConnectionListener connectionListener : mGlobalConnectionListeners) {
                                        connectionListener.onDisconnected();

                                    }
                                }

                                synchronized (mConnectingListeners) {
                                    Set<ConnectionListener> connectionListeners = mConnectingListeners.get(device.getAddress());
                                    if (connectionListeners != null && connectionListeners.size() > 0) {
                                        for (final ConnectionListener connectionListener : connectionListeners) {
                                            connectionListener.onDisconnected();
                                        }
                                    }
                                }
                            });
                            break;
                        case BleConst.CONNECT_FAIL:
                            mExecutorService.execute(() -> {
                                synchronized (mGlobalConnectionListeners) {
                                    for (ConnectionListener connectionListener : mGlobalConnectionListeners) {
                                        connectionListener.onConnectFail();
                                    }
                                }

                                synchronized (mConnectingListeners) {
                                    Set<ConnectionListener> connectionListeners = mConnectingListeners.get(device.getAddress());
                                    if (connectionListeners != null && connectionListeners.size() > 0) {
                                        Iterator<ConnectionListener> it = connectionListeners.iterator();
                                        while (it.hasNext()) {
                                            ConnectionListener connectionListener = it.next();
                                            if (connectionListener != null) {
                                                connectionListener.onConnectFail();
                                            }
                                        }
                                        // for (final ConnectionListener connectionListener : connectionListeners) {
                                        //     connectionListener.onConnectFail();
                                        // }
                                    }
                                }
                            });
                            break;
                        case BleConst.CONNECT_DISCONNECTING:
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
