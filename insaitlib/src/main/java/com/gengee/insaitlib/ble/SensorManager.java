package com.gengee.insaitlib.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gengee.insaitlib.ble.core.BleConnection;
import com.gengee.insaitlib.ble.dic.BleDeviceType;
import com.gengee.insaitlib.ble.model.BatteryInfo;
import com.gengee.insaitlib.ble.util.BleConst;
import com.gengee.insaitlib.ble.util.LogUtil;

public class SensorManager {
    private static final String TAG = "SensorManager";

    private static SensorManager mInstance;
    private BaseSensorService.BaseSensorBinder mBinder;
    protected boolean hadBindService;

    /**
     * Creates a static instance of this class that could be used throughout the application lifecycle.
     */
    public static SensorManager getInstance() {
        if (mInstance == null) {
            synchronized (SensorManager.class) {
                if (mInstance == null) {
                    mInstance = new SensorManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * Clears the static instance of this class created by @link {@link #getInstance()} when the application is finishing.
     */
    public static SensorManager clearInstance() {
        return mInstance = null;
    }

    private SensorManager() {
        // empty constructor
    }

    /**
     * Service connection listener interface
     */
    public interface ServiceConnectionListener {
        /**
         * Called when the the service is connected to the activity.
         */
        void onServiceConnected();
    }

    public byte resolveState(byte data, BluetoothDevice device, BleDeviceType deviceType) {
        return BleConnection.resolveState(data, deviceType);
    }

    public byte resolveState(byte data, BluetoothDevice device) {
        if (device != null && data != 0 && mBinder != null) {
            BleConnection bleConnection = mBinder.getBleConnection(device);
            if (bleConnection != null) {
                return BleConnection.resolveState(data, bleConnection.getDeviceType());
            }
        }
        return 0;
    }

    /**
     * Service connection is maintained in this class
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (BaseSensorService.BaseSensorBinder) service;
            if (mBinder != null) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i(TAG, "unbound");
//            mBinder = null;
        }
    };

    /**
     * Bind to the base service. This will start the BaseThingyService as a started service and then bind to it.
     * Implement @link {@link ServiceConnectionListener} in the activity to get the service @link
     * {@link ServiceConnection onServiceConnected callbacks}
     *
     * @param context required context to call unbind from
     */
    public void bindService(final Context context, final Class<? extends BaseSensorService> service,ServiceConnectionListener serviceConnectionListener) {
//        if (!hadBindService) {
            final Intent intent = new Intent(context, service);
            context.startService(intent);
            context.bindService(intent, mServiceConnection, 0);
            hadBindService = true;
//        }
    }

    /**
     * Unbind from the base service
     * used to unbind the service from the activity when switching between activities or when the app goes to background
     *
     * @param context required context to call unbind from
     */
    public void unbindService(final Context context) {
        if (mServiceConnection != null) {
            context.unbindService(mServiceConnection);
//            hadBindService = false;
        }
    }

    /**
     * Stop the background service
     * Ensure @link {@link #unbindService(Context)} is called first since the background service is not a bound service
     *
     * @param context required context to call unbind from
     * @param service to be stopped
     */
    public void stopService(final Context context, final Class<? extends BaseSensorService> service) {
        //Since we are stopping the service we must make sure to disconnect from all thingy devices
        disconnectFromAllSensors();

        //call stop service since the initial service was not a bound service
        Intent i = new Intent(context, service);
        context.stopService(i);
    }

    /**
     * Returns a Binder object for the specifie bluetooth device
     * Use this binder to access your own implementation after extending the @link
     * {@link BaseSensorService.BaseSensorBinder}
     */
    public BaseSensorService.BaseSensorBinder getThingyBinder() {
        return mBinder;//.getThingyServiceBinder();
    }

    /**
     * Returns a ThingyConnection object for the specifie bluetooth device (Thingy)
     *
     * @param device is the unique id for a ThingyConnection stored in a map
     */
    public BleConnection getBleConnection(final BluetoothDevice device) {
        if (device != null) {
            if (mBinder != null) {
                return mBinder.getBleConnection(device);
            }
        }
        return null;
    }

    /**
     * Connects to a particular thingy. This method will start the thingy service and pass the requested device using the intent extras
     * This will connect the thingy and do a complete service discovery of all service and characteristics available on Thingy:52.
     *
     * @param context context
     * @param device  Bluetooth device to connect to
     * @param service service class
     */
    public void connectToThingy(final Context context, final BluetoothDevice device, final Class<? extends BaseSensorService> service) {
        final Intent intent = new Intent(context, service);
        intent.putExtra(BleConst.EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public void reconnect(final Context context,final Class<? extends BaseSensorService> service){
        connectToThingy(context,getSelectedDevice(),service);
    }

    /**
     * Disconnect from all thingies
     */
    public void disconnectFromAllSensors() {
        if (mBinder != null) {
            mBinder.disconnectAllDevices();
        }
    }

    /**
     * Disconnects from a particular thingy
     *
     * @param device bluetooth device to disconnect from
     */
    public void disconnectFromThingy(final BluetoothDevice device) {
        if (mBinder != null) {
            mBinder.disconnectDevice(device);
        }
    }

    /**
     * Returns the connections state a device
     */
    public boolean isConnected(final BluetoothDevice device) {
//        if (device != null) {
//            if (mBinder != null) {
//                final BleConnection bleConnection = mBinder.getBleConnection(device);
//                if (bleConnection != null) {
//                    return bleConnection.isConnected();
//                }
//            }
//        }
        if (device == null) {
            return false;
        }
        //得到BluetoothDevice的Class对象
        Class<BluetoothDevice> bluetoothDeviceClass = BluetoothDevice.class;
        try {//得到连接状态的方法
            Method method = bluetoothDeviceClass.getDeclaredMethod("isConnected", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            return (boolean) method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the connections state of select device
     */
    public boolean isConnected() {
        if (getSelectedDevice() != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(getSelectedDevice());
                if (bleConnection != null) {
                    return bleConnection.isConnected();
                }
            }
        }
        return false;
    }

    public boolean isConnected(BleDeviceType deviceType) {
        if (getSelectedDevice() != null) {
            if (mBinder != null) {
                BluetoothDevice bleDevice = getSelectedDevice();
                if (bleDevice != null) {
                    if (isValidSensor(bleDevice.getName(), deviceType)) {
                        final BleConnection bleConnection = mBinder.getBleConnection(bleDevice);
                        if (bleConnection != null) {
                            return bleConnection.isConnected();
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public boolean isValidSensor(String name, BleDeviceType deviceType) {
        String[] prefixes = BleConst.PREFIX_LIST_SHIN;
        if (deviceType == BleDeviceType.FOOTBALL) {
            prefixes = BleConst.PREFIX_LIST;
        }
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        boolean isValid = false;
        for (String prefixBle : prefixes) {
            if (name.startsWith(prefixBle)) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }


    /**
     * Selects the current bluetooth device and is stored in the base service
     *
     * @param device bluetooth device to be selected
     */
    public void setSelectedDevice(BluetoothDevice device) {
        if (mBinder != null) {
            if (device != null) {
                mBinder.setSelectedDevice(device);
            }
        }
    }

    /**
     * Returns the current bluetooth device which was selected from {@link #setSelectedDevice(BluetoothDevice)}.
     */
    public BluetoothDevice getSelectedDevice() {
        if (mBinder != null) {
            return mBinder.getSelectedDevice();
        }
        return null;
    }

    public BatteryInfo getBatteryInfo(final BluetoothDevice device) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(device);
                if (bleConnection != null) {
                    return bleConnection.getBatteryInfo();
                }
            }
        }
        return null;
    }

    public String getFirmwareVersion(final BluetoothDevice device) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(device);
                if (bleConnection != null) {
                    return bleConnection.getFirmwareVersion();
                }
            }
        }
        return null;
    }

    /**
     * Returns the list of connected devices
     */
    public List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        if (mBinder != null) {
            devices.addAll(mBinder.getConnectedDevices());
        }
        return devices;
    }

    public BatteryInfo getSelectBatteryInfor(){
        if(mBinder!=null){
            final BleConnection thingyConnection = mBinder.getBleConnection(getSelectedDevice());
            if (thingyConnection != null) {
                return thingyConnection.getBatteryInfo();
            }
        }
        return null;
    }
    public boolean isBleTurnOn(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                return false;
            }
            return bluetoothAdapter.isEnabled();
        }

        return false;
    }

    public boolean isUserDisconnected(){
        if(mBinder!=null){
            final BleConnection bleConnection = mBinder.getBleConnection(getSelectedDevice());
            if(bleConnection!=null){
                return bleConnection.isUserDisconnected();
            }
        }
        return false;
    }

    public String getBleName(){
        return getBleName(getSelectedDevice());
    }
    public String getDeviceShortName(){
        if (getSelectedDevice() != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(getSelectedDevice());
                if (bleConnection != null) {
                    return bleConnection.getDeviceShortName();
                }
            }
        }
        return null;
    }

    public String getBleName(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(bluetoothDevice);
                if (bleConnection != null) {
                    return bleConnection.getBleName();
                }
            }
        }
        return null;
    }
    /**
     * Configure a device name for the thingy which would be used for advertising
     *
     * @param device     bluetooth device
     * @param deviceName device name to be set
     */
    public void setDeviceName(final BluetoothDevice device, final String deviceName) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(device);
                if (bleConnection != null) {
                    bleConnection.setDeviceName(deviceName);
                }
            }
        }
    }

    /**
     * Returns the device name for the specific thingy
     */
    public String getDeviceName(final BluetoothDevice device) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection thingyConnection = mBinder.getBleConnection(device);
                if (thingyConnection != null) {
                    return thingyConnection.getDeviceShortName();
                }
            }
        }
        return null;
    }

    public void joinWriteCharaCommand(final BluetoothDevice device, UUID serverUuid, UUID charaUuid, byte[] command) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(device);
                if (bleConnection != null) {
                    bleConnection.joinWriteCharaCommand(serverUuid, charaUuid, command);
                } else {
                    LogUtil.e(TAG, "写入设备失败，bleConnection = null");
                }
            } else {
                LogUtil.e(TAG, "写入设备失败 mBinder = null");
            }
        } else {
            LogUtil.e(TAG, "写入设备失败 device = null");
        }
    }

    public void joinReadCharaCommand(final BluetoothDevice device, UUID serverUuid, UUID charaUuid) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(device);
                if (bleConnection != null) {
                    bleConnection.joinReadCharaCommand(serverUuid, charaUuid);
                    LogUtil.d(TAG, "发送读取设备信息指令");
                } else {
                    LogUtil.e(TAG, "发送读取设备信息指令失败，bleConnection = null address:" + device.getAddress());
                }
            } else {
                LogUtil.e(TAG, "发送读取设备信息指令失败 mBinder = null");
            }
        } else {
            LogUtil.e(TAG, "发送读取设备信息指令失败 device = null");
        }
    }

    public void joinNotifyCommand(final BluetoothDevice device, UUID serverUuid, UUID charaUuid) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(device);
                if (bleConnection != null) {
                    bleConnection.joinNotifyCommand(serverUuid, charaUuid);
                } else {
                    LogUtil.e(TAG, "设置设备通知失败，bleConnection = null address:" + device.getAddress());
                }
            } else {
                LogUtil.e(TAG, "设置设备通知失败 mBinder = null");
            }
        } else {
            LogUtil.e(TAG, "设置设备通知失败 device = null");
        }
    }

    public void joinAlgorithmCommand(final BluetoothDevice device, UUID serverUuid, UUID charaUuid, BleConnection.AlgorithmCallback callback) {
        if (device != null) {
            if (mBinder != null) {
                final BleConnection bleConnection = mBinder.getBleConnection(device);
                if (bleConnection != null) {
                    bleConnection.joinAlgorithmCommand(serverUuid, charaUuid, callback);
                } else {
                    LogUtil.e(TAG, "设置设备算法通知失败，bleConnection = null address:" + device.getAddress());
                }
            } else {
                LogUtil.e(TAG, "设置设备算法通知失败 mBinder = null");
            }
        } else {
            LogUtil.e(TAG, "设置设备算法通知失败 device = null");
        }
    }

    public void joinWriteCharaCommand(UUID serverUuid, UUID charaUuid, byte[] command) {
        joinWriteCharaCommand(getSelectedDevice(), serverUuid, charaUuid, command);
    }

    public void joinReadCharaCommand(UUID serverUuid, UUID charaUuid) {
        joinReadCharaCommand(getSelectedDevice(),serverUuid,charaUuid);
    }

    public void joinNotifyCommand(UUID serverUuid, UUID charaUuid) {
        joinNotifyCommand(getSelectedDevice(),serverUuid,charaUuid);
    }

}
