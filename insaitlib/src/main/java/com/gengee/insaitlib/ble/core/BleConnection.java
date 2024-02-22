package com.gengee.insaitlib.ble.core;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gengee.insaitlib.ble.dic.BleDeviceType;
import com.gengee.insaitlib.ble.helper.CommandNode;
import com.gengee.insaitlib.ble.helper.CommandQueue;
import com.gengee.insaitlib.ble.model.BatteryInfo;
import com.gengee.insaitlib.ble.util.BleConst;
import com.gengee.insaitlib.ble.util.BlePenConst;
import com.gengee.insaitlib.ble.util.BleShinConst;
import com.gengee.insaitlib.ble.util.DataUtil;
import com.gengee.insaitlib.ble.util.LogUtil;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("MissingPermission")
public class BleConnection {
    private static final String TAG = "BleConnection";

    protected BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;

    private final Object mLock = new Object();

    private final Context mContext;

    private final Handler mHandler;

    protected final BleManagerCallbacks mCallbacks;

    private BleManagerGattCallback mGattCallback;

    /**
     * Flag set to true when {@link #shouldAutoConnect()} method returned <code>true</code>. The first connection attempt is done with
     * <code>autoConnect</code>
     * flag set to false (to make the first connection quick) but on connection lost the manager will call
     * {@link #connect(BluetoothDevice)} (BluetoothDevice)} again.
     * This time this method will call {@link BluetoothGatt#connect()} which always uses <code>autoConnect</code> equal true.
     */
    private boolean mInitialConnection;
    /**
     * This flag is set to false only when the {@link #shouldAutoConnect()} method returns true and the device got disconnected without calling
     * <p>
     * If {@link #shouldAutoConnect()} returns false (default) this is always set to true.
     */
    private boolean mUserDisconnected;

    /**
     * Flag set to true when the device is connected.
     */
    private boolean mConnected;

    private int mConnectionState = BluetoothGatt.STATE_DISCONNECTED;

    protected boolean isRequestRefreshCache;

    /**
     * 电池电量
     */
    protected BatteryInfo mBatteryInfo;
    //  firmware version
    protected String mFirmwareVersion;

    protected boolean isUpgrading;
    protected boolean isSyncing;

    protected BleDeviceType mDeviceType;

    private volatile boolean mIsTimeoutRunning = false;

    public BleConnection(final Context context, BleDeviceType deviceType, final BluetoothDevice bluetoothDevice, BleManagerCallbacks callbacks) {
        this.mContext = context;

        this.mBluetoothDevice = bluetoothDevice;

        this.mDeviceType = deviceType;

        mCallbacks = callbacks;

        mHandler = new Handler(Looper.getMainLooper());

        mBatteryInfo = new BatteryInfo();

        mConnectCount = 0;

        if (bluetoothDevice != null) {
            LogUtil.e(TAG, "BleConnection: new with device " + bluetoothDevice.getName());
        }
        try {
            mContext.unregisterReceiver(mBluetoothStateBroadcastReceiver);
        } catch (Exception e) {
            // the receiver must have been not registered or unregistered before
        }

        mContext.registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        connect(bluetoothDevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBluetoothDevice.getAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BleConnection connection = (BleConnection) o;
        return Objects.equals(mBluetoothDevice.getAddress(), connection.getCurrentDevice().getAddress());
    }

    protected String mBleName;

//    protected Thread mTimeoutThread;
    protected Timer mTimer;
    protected TimerTask mTimerTask;

    public void setBleName(String bleName) {
        mBleName = bleName;
    }

    public String getBleName() {
        return mBleName;
    }

    /**
     * Connects to the Bluetooth Smart device.
     *
     * @param device a device to connect to
     */
    public int connect(final BluetoothDevice device) {
        LogUtil.d(TAG, "connect start");
        if (mConnected && mConnectionState == BluetoothGatt.STATE_CONNECTED) {
            LogUtil.d(TAG, "is connecting");
            return 1;
        }
        LogUtil.d(TAG, "connect()==>" + device);
        mIsTimeoutRunning = false;
//        if (mTimeoutThread != null) {
//            mTimeoutThread.interrupt();
//            mTimeoutThread = null;
//        }
        if (mTimer != null) mTimer.cancel();

        synchronized (mLock) {
            if (mBluetoothGatt != null) {
                // There are 2 ways of reconnecting to the same device:
                // 1. Reusing the same BluetoothGatt object and calling connect() on it or
                // 2. Closing it and reopening a new instance of BluetoothGatt object.
                // The gatt.close() is an asynchronous method. It requires some time before it's finished and
                // device.connectGatt(...) can't be called immediately or service discovery
                // may never finish on some older devices (Nexus 4, Android 5.0.1).
                // However, the autoConnect flag settings may have changed. If it did, try closing and opening new BluetoothGatt
                // despite the difficulties.
                if (!mInitialConnection) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    try {
                        Thread.sleep(200); // Is 200 ms enough?
                    } catch (final InterruptedException e) {
                        // Ignore
                    }
                } else {
                    // Instead, the gatt.connect() method will be used to reconnect to the same device.
                    // This method forces autoConnect = true even if the gatt was created with this flag set to false.
                    mInitialConnection = false;
                    mConnectionState = BluetoothGatt.STATE_CONNECTING;
                    mCallbacks.onDeviceConnecting(device);
                    mBluetoothGatt.connect();
                    return 0;
                }
            }
        }

        mUserDisconnected = false; // We will receive Linkloss events only when the device is connected with autoConnect=true
        mBluetoothDevice = device;
        mConnectionState = BluetoothGatt.STATE_CONNECTING;
        mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback = new BleManagerGattCallback());
        mCallbacks.onDeviceConnecting(device);
        mConnected = false;

        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            long countdown = 0;

            @Override
            public void run() {
                mIsTimeoutRunning = true;
                countdown++;
                BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager != null && device != null) {
                    LogUtil.e("BleConnection", device.getName() + " "
                            + bluetoothManager.getConnectionState(device, BluetoothProfile.GATT)
                            + " countdown = " + countdown);
                }

                if (countdown >= 15) {
                    if (!mConnected && mIsTimeoutRunning) {
                        doDisconnect(false);
                        LogUtil.e(TAG, (device != null ? device.getName() : "") + " thread connect ble timeout");
                        mCallbacks.onLinklossOccur(device);
                    }

                    close();
                }
            }
        };
        mTimer.schedule(mTimerTask, 1000);

//        mTimeoutThread = new Thread(new Runnable() {
//            long countdown = 0;
//            @Override
//            public void run() {
//                mIsTimeoutRunning = true;
//                while (true) {
//                    try {
//                        Thread.sleep(1000);
////                        if (!mIsTimeoutRunning) {
////                            break;
////                        }
//                        countdown++;
//                        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
//                        if (bluetoothManager != null) {
//                            LogUtil.e("BleConnection", device.getName() + " "
//                                    + bluetoothManager.getConnectionState(device, BluetoothProfile.GATT)
//                                    + " countdown = " + countdown);
//                        }
//
//                        if (countdown >= 15) {
//                            if (!mConnected && mIsTimeoutRunning) {
//                                doDisconnect(false);
//                                LogUtil.e(TAG, device.getName() + " thread connect ble timeout");
//                                mCallbacks.onLinklossOccur(device);
////                                mCallbacks.onDeviceDisconnected(device, false);
//                                break;
//                            }
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        LogUtil.e("BleConnection", device.getName() + " printStackTrace");
//                        break;
//                    }
//
//                }
//            }
//        });
//        LogUtil.d(TAG, device.getName() + " thread connect ble timeout start");
//        mTimeoutThread.start();
        setBleName(device.getName());
        return 0;
    }

    public boolean isUserDisconnected() {
        return mUserDisconnected;
    }

    public void setUpgrading(boolean upgrading) {
        isUpgrading = upgrading;
    }

    public void setSyncing(boolean syncing) {
        this.isSyncing = syncing;
    }

    protected final Object mConnectLock = new Object();

    /**
     * Disconnects from the device. Does nothing if not connected.
     *
     */
    public void doDisconnect(boolean isUserDisconnect) {
        synchronized (mConnectLock) {
            mUserDisconnected = isUserDisconnect;
            mInitialConnection = false;
            isAutoConnect = false;
            mCommandQueue.clearAll();
            mIsTimeoutRunning = false;
//            if (mTimeoutThread != null) {
//                mTimeoutThread.interrupt();
//                mTimeoutThread = null;
//            }
            if (mTimer != null) mTimer.cancel();
            close();
        }

    }


    /**
     * Closes and releases resources. May be also used to unregister broadcast listeners.
     */
    public void close() {
        try {
            mContext.unregisterReceiver(mBluetoothStateBroadcastReceiver);
        } catch (Exception e) {
            // the receiver must have been not registered or unregistered before
        }
        synchronized (mLock) {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
//                mBluetoothGatt.close();
//                mBluetoothGatt = null;
            }
            mConnected = false;
            mInitialConnection = false;
            mConnectionState = BluetoothGatt.STATE_DISCONNECTED;
            mGattCallback = null;
            mBluetoothDevice = null;
            if (mTimer != null) mTimer.cancel();
        }
    }

    public boolean isConnected() {
        return mConnected && mConnectionState == BluetoothGatt.STATE_CONNECTED;
    }

    protected boolean isAutoConnect = true;

    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    if (mConnected && previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        // The connection is killed by the system, no need to gently disconnect
                        mGattCallback.notifyDeviceDisconnected(mBluetoothDevice);
                    }
                    close();
                    break;
            }
        }
    };


    /**
     * 用户设置的设备名称，即不包含 WICORE 前缀。
     */
    protected String mShortName;

    //命令队列，因为不能同时发送指令，所以采用队列方式进行发送
    protected CommandQueue mCommandQueue = new CommandQueue();


    protected final Object mSendLock = new Object();

    //是否命令发送中
    protected boolean isRequesting;


    // 电池状态
    private void onBatteryStateChanged(byte[] payload) {
        int volume = payload[0] & 0xff;
        int state = payload[1] & 0xff;

        LogUtil.d(TAG, "电量 = " + volume + " state状态：" + state);

        mBatteryInfo.setState(state);
        mBatteryInfo.setVolume(volume);

        final Intent intent = new Intent(BleConst.NOTIFICATION_BATTERY);
        intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
        intent.putExtra(BleConst.EXTRA_DATA, mBatteryInfo);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onPenBatteryStateChanged(byte[] payload) {
        if (payload.length >= 3) {
            int volume = payload[2] & 0xff;
            int state = payload[1] & 0xff;

            LogUtil.d(TAG, "电量 = " + volume + " state状态：" + state);

            mBatteryInfo.setState(state);
            mBatteryInfo.setVolume(volume);

            final Intent intent = new Intent(BleConst.NOTIFICATION_BATTERY);
            intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(BleConst.EXTRA_DATA, mBatteryInfo);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    private void onFirmwareVersionChanged(byte[] data) {
        byte[] bytes = new byte[5];
        System.arraycopy(data, 0, bytes, 0, 5);
        mFirmwareVersion = new String(bytes);

        final Intent intent = new Intent(BleConst.NOTIFICATION_NORMAL_READ);
        intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
        intent.putExtra(BleConst.EXTRA_DATA, data);
        intent.putExtra(BleConst.EXTRA_COMMAND_UUID, BleConst.UUID_CHARA_R_FIRMWARE_VERSION);
        intent.putExtra(BleConst.EXTRA_COMMAND_RESULT, true);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onPenFirmwareVersionChanged(byte[] data) {
        byte[] bytes = new byte[5];
        System.arraycopy(data, 0, bytes, 0, 5);
        mFirmwareVersion = new String(bytes);

        final Intent intent = new Intent(BleConst.NOTIFICATION_NORMAL_READ);
        intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
        intent.putExtra(BleConst.EXTRA_DATA, data);
        intent.putExtra(BleConst.EXTRA_COMMAND_UUID, BlePenConst.UUID_CHARA_R_FIRMWARE_VERSION);
        intent.putExtra(BleConst.EXTRA_COMMAND_RESULT, true);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onSensorStateChange(UUID uuid, byte[] data) {
        int state = -1;
        int type = -1;
        if (data != null) {
            if (data.length > 1) {
                state = data[0];
                type = data[1];
            } else {
                //旧版一字节，低4位代表状态
                state = (data[0] >> 4) & 0x0f;
                type = data[0] & 0x0f;
            }
        }

        state = resolveState((byte) state, mDeviceType);
        LogUtil.d(TAG, "设备状态：" + state + " type=" + type);

        final Intent intent = new Intent(BleConst.NOTIFICATION_DEVICE_STATE);
        intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
        intent.putExtra(BleConst.EXTRA_COMMAND_UUID, uuid);
        intent.putExtra(BleConst.EXTRA_SENSOR_STATE, state);
        intent.putExtra(BleConst.EXTRA_SENSOR_TYPE, type);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public static byte resolveState(byte data, BleDeviceType type) {
        if (type == BleDeviceType.FOOTBALL) {
            return data;
        } else if (type == BleDeviceType.SHINGUARD) {
            return resolveBindState(data);
        }
        return 0;
    }

    public static byte resolveBindState(byte data) {
        //0未配对，1 未绑定，3绑定状态
        //绑定状态二进制值：11为绑定状态
        if ((data & 0x02) > 0) {
            return 3;
        } else if ((data & 0x01) > 0) {
            return 1;
        }
        return 0;
    }

    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    public BleDeviceType getDeviceType() {
        return mDeviceType;
    }

    /**
     * 获取指定设备的电池电量
     */
    public BatteryInfo getBatteryInfo() {
        return mBatteryInfo;
    }

    /**
     * name的字节数范围[1-12]byte
     */
    public void setDeviceName(String name) {
        byte[] bytes = name.getBytes();
        if (bytes.length == 0 || bytes.length > 12) {
            return;
        }
        mShortName = name;
        switch (mDeviceType) {
            case FOOTBALL:
                byte[] command = DataUtil.makeCommand(BleConst.SENSOR_CONF_LAST_NAME, name.getBytes());
                joinWriteCharaCommand(BleConst.UUID_SERVICE_SENSOR_DATA, BleConst.UUID_CHARA_W_SET_DEVICE, command);
                break;
            case SHINGUARD:
                command = DataUtil.makeCommand(BleShinConst.SENSOR_CONF_LAST_NAME, name.getBytes());
                joinWriteCharaCommand(BleConst.UUID_SERVICE_SENSOR_DATA, BleConst.UUID_CHARA_W_SET_DEVICE, command);
                break;
            default:
                break;
        }

    }

    // 在第一次连接时从gatt中获取deviceName。即使之后通过setCharacteristic 设置了
    // 新的球名，gatt中保存的deviceName却不会同时更新。因此用mShortName保存用户设置的球名。
    public String getDeviceShortName() {
        return mShortName;
    }

    public BluetoothDevice getCurrentDevice() {
        return mBluetoothDevice;
    }

    protected boolean shouldAutoConnect() {
        return true;
    }

    public void joinWriteCharaCommand(UUID serverUuid, UUID charaUuid, byte[] command) {
        synchronized (mSendLock) {
            CommandNode commandNode = new CommandNode(CommandNode.CommandType.WriteChara, serverUuid, charaUuid, command);
            mCommandQueue.joinQueue(commandNode);
            if (isRequesting) {
                return;
            }
            isRequesting = true;
            mHandler.post(mProcessNextTask);
        }
    }

    public void joinReadCharaCommand(UUID serverUuid, UUID charaUuid) {

        synchronized (mSendLock) {
            CommandNode commandNode = new CommandNode(CommandNode.CommandType.ReadChara, serverUuid, charaUuid, null);
            mCommandQueue.joinQueue(commandNode);
            if (isRequesting) {
                return;
            }
            isRequesting = true;
            mHandler.post(mProcessNextTask);
        }

    }

    public void joinNotifyCommand(UUID serverUuid, UUID charaUuid) {
        synchronized (mSendLock) {
            CommandNode commandNode = new CommandNode(CommandNode.CommandType.Notify, serverUuid, charaUuid, null);
            mCommandQueue.joinQueue(commandNode);
            if (isRequesting) {
                return;
            }
            isRequesting = true;
            mHandler.post(mProcessNextTask);
        }
    }

    public void joinAlgorithmCommand(UUID serverUuid, UUID charaUuid, AlgorithmCallback callback) {
        synchronized (mSendLock) {
            CommandNode commandNode = new CommandNode(CommandNode.CommandType.Notify, serverUuid, charaUuid, null);
            mCommandQueue.joinQueue(commandNode);
            mAlgorithmCallback = callback;
            if (isRequesting) {
                return;
            }
            isRequesting = true;
            mHandler.post(mProcessNextTask);
        }
    }

    protected boolean setCharacterNotification(BluetoothGatt gatt, BluetoothGattCharacteristic chr) {
        boolean dataNotify = gatt.setCharacteristicNotification(chr, true);

        BluetoothGattDescriptor descr = chr.getDescriptor(BleConst.CLIENT_CHARACTERISTIC_CONFIG);
        descr.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        dataNotify &= gatt.writeDescriptor(descr);
        return dataNotify;
    }

    protected boolean internalEnableNotifications(UUID serviceUUid, UUID commandUuid) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            LogUtil.i(TAG, "internalEnableNotifications gatt == null");
            return false;
        }
        BluetoothGattService service = gatt.getService(serviceUUid);
        if (service == null) {
            LogUtil.i(TAG, "internalEnableNotifications service == null serviceUUid=" + serviceUUid);
            return false;
        }

        // 开启传感器数据notify
        BluetoothGattCharacteristic chr = service.getCharacteristic(commandUuid);
        if (chr == null) {
            LogUtil.i(TAG, "internalEnableNotifications Characteristic == null commandUuid=>" + commandUuid);
            return false;
        }

        boolean notify = setCharacterNotification(gatt, chr);
        LogUtil.e(TAG, "Notification = " + notify);
        return notify;
    }

    protected boolean internalWriteCharacteristic(UUID serverUuid, UUID charaUuid, byte[] command) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            LogUtil.i(TAG, "sendWriteCharaCommand fail! gatt == null");
            return false;
        }
        BluetoothGattService service = gatt.getService(serverUuid);
        if (service == null) {
            LogUtil.i(TAG, "sendWriteCharaCommand fail! service == null");
            return false;
        }
        BluetoothGattCharacteristic chr = service.getCharacteristic(charaUuid);
        if (chr != null) {
            chr.setValue(command);
            gatt.writeCharacteristic(chr);
            return true;
        }
        return false;
    }

    protected boolean internalReadCharacteristic(UUID serverUuid, UUID charaUuid) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) {
            LogUtil.e(TAG, "sendReadCharaCommand fail! == null");
            return false;
        }
        BluetoothGattService service = gatt.getService(serverUuid);
        if (service == null) {
            LogUtil.e(TAG, "sendReadCharaCommand fail! service == null");
            return false;
        }
        BluetoothGattCharacteristic chr = service.getCharacteristic(charaUuid);
        if (chr != null) {
            gatt.readCharacteristic(chr);
            LogUtil.d(TAG, "写入成功serverUuid：" + serverUuid + " charaUuid=" + charaUuid);
            return true;
        }
        return false;
    }

    /**
     * Clears the internal cache and forces a refresh of the services from the
     * remote device.
     * 主要是固件修改一些跟UUID状态有关的，需要清除缓存
     */
    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        LogUtil.d(TAG, "开始清理蓝牙缓存");
        if (gatt != null) {
            try {
                Method localMethod = gatt.getClass().getMethod("refresh", new Class[0]);
                boolean bool = (Boolean) localMethod.invoke(gatt, new Object[0]);
                LogUtil.d(TAG, "蓝牙缓存清理成功.");
                return bool;
            } catch (Exception localException) {
                LogUtil.e(TAG, "清理蓝牙缓存失败." + localException.getMessage());
            }
        }
        return false;
    }

    /**
     * Runnable used to push processing ble requests to the a ui thread.
     * This is due to samsung galaxy devices and huawei nexus 6P had a synchronizing issue
     * when process next was called from the same thread as the callbacks which was noticed during audio streaming
     */
    private Runnable mProcessNextTask = new Runnable() {
        @Override
        public void run() {
            CommandNode commandNode = mCommandQueue.removeFirstQueue();
            if (commandNode != null) {
                boolean isOk = nextRequest(commandNode);
                //如果发送失败，直接执行下一个指令
                if (!isOk) {
                    mHandler.post(mProcessNextTask);
                    LogUtil.e(TAG, "命令发送失败");
                } else {
                    LogUtil.d(TAG, "命令发送中");
                }
            } else {
                synchronized (mSendLock) {
                    isRequesting = false;
                }
            }
        }
    };

    /**
     * Executes the next request. If the last element from the initialization queue has been executed
     * the {@link BleManagerCallbacks#onDeviceReady(BluetoothDevice)} callback is called.
     */
    private synchronized boolean nextRequest(CommandNode commandNode) {
        boolean result = false;
        switch (commandNode.commandType) {
            case Notify: {
                result = internalEnableNotifications(commandNode.serviceUUID, commandNode.charaUUID);
                break;
            }
            case ReadChara: {
                result = internalReadCharacteristic(commandNode.serviceUUID, commandNode.charaUUID);
                break;
            }
            case WriteChara: {
                result = internalWriteCharacteristic(commandNode.serviceUUID, commandNode.charaUUID, commandNode.commandBytes);
                break;
            }
            case READ_DESCRIPTOR:
            case WRITE_DESCRIPTOR: {
                break;
            }
        }

        return result;

    }

    protected int mConnectCount;
    protected AlgorithmCallback mAlgorithmCallback;

    public interface AlgorithmCallback {
        void onDataUpdated(BluetoothDevice bluetoothDevice, byte[] data);
    }

    private final class BleManagerGattCallback extends BluetoothGattCallback {
        private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
        private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
        private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
        private final static String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
        private final static String ERROR_READ_DESCRIPTOR = "Error on reading descriptor";
        private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";

        private void notifyDeviceDisconnected(final BluetoothDevice device) {
            mConnected = false;
            mConnectionState = BluetoothGatt.STATE_DISCONNECTED;
            if (mUserDisconnected) {
                mCallbacks.onDeviceDisconnected(device, true);
                close();
            } else {
//                mCallbacks.onDeviceDisconnected(device, false);
                mCallbacks.onLinklossOccur(device);
                // We are not closing the connection here as the device should try to reconnect automatically.
                // This may be only called when the shouldAutoConnect() method returned true.
            }
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }

//            if (mProfile != null)
//                mProfile.release();
        }

        private void onError(final BluetoothDevice device, final String message, final int errorCode) {
            mCallbacks.onError(device, message, errorCode);
//            if (mProfile != null)
//                mProfile.onError(message, errorCode);
        }

        @Override
        public final void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {

            if (isUpgrading || isSyncing) {
                return;
            }

            String deviceName = gatt.getDevice().getName();

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                LogUtil.d(TAG, deviceName + " 设备已连接！");
                long delayTime = DelaySearchServiceTime;
                if (isRequestRefreshCache) {
                    //刷新缓存
                    if (refreshDeviceCache(gatt)) {
                        isRequestRefreshCache = false;
                        delayTime = 3000;

                        // Notify the parent activity/service
                        mConnected = true;
                        mConnectionState = BluetoothGatt.STATE_CONNECTED;

                        mFindServicesCount = 0;

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Some proximity tags (e.g. nRF PROXIMITY) initialize bonding automatically when connected.
                                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                                    gatt.discoverServices();
                                    LogUtil.d(TAG,  "开始查找服务！");
                                } else {
                                    LogUtil.e(TAG, "启动查找服务失败，设备未bond");
                                }
                            }
                        }, delayTime);
                        return;
                    }
                }
                // Notify the parent activity/service
                mConnected = true;
                mConnectionState = BluetoothGatt.STATE_CONNECTED;

                mFindServicesCount = 0;

                //是否已配对
                final boolean bonded = gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED;
                //部分手机时间太短服务发现不了
                final long delay = bonded ? delayTime : 200; // around 1600 ms is required when connection interval is ~45ms.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Some proximity tags (e.g. nRF PROXIMITY) initialize bonding automatically when connected.
                        if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                            gatt.discoverServices();
                            LogUtil.d(TAG, "开始查找服务！");
                        } else {
                            LogUtil.e(TAG, "启动查找服务失败，设备未bond");
                        }
                    }
                }, delay);
            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    synchronized (mSendLock) {
                        isRequesting = false;
                        mCommandQueue.clearAll();
                    }
                    synchronized (mConnectLock) {
                        LogUtil.e(TAG, deviceName + "设备已断开连接!");
                        // Try to reconnect if the initial connection was lost because of a link loss or timeout, and shouldAutoConnect() returned
                        // true
                        // during connection attempt.
                        // This time it will set the autoConnect flag to true (gatt.connect() forces autoConnect true)
                        mConnectionState = BluetoothGatt.STATE_DISCONNECTED;

//                        if (!mUserDisconnected) {
//                            if (isAutoConnect && SensorManager.getInstance().isBleTurnOn(mContext)) {
//                                //三星手机马上连接有问题
//                                mHandler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        synchronized (mConnectLock) {
//                                            if (isAutoConnect) {
//                                                connect(gatt.getDevice(), true);
//                                            }
//                                        }
//                                    }
//                                }, 500);
//                                return;
//                            }
//                        }
//                        if (mConnected) {
                        notifyDeviceDisconnected(gatt.getDevice()); // This sets the mConnected flag to false
//                        }
                    }

                }
            }
        }

        protected final int SEARCH_SERVICE_MAX = 5;//服务未发现，重复查询服务最多次数
        protected final int DelaySearchServiceTime = 1000;//查询一次服务间隔时间
        protected int mFindServicesCount;

        @Override
        public final void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                final boolean hasProfile = BleProfileProvider.findProfile(gatt);
                if (hasProfile) {//发现服务才算连接成功
                    LogUtil.d(TAG, "设备服务查找成功！");
                    //  获取电池电量 和 固件版本号
                    joinReadCharaCommand(BlePenConst.UUID_SERVICE_PEN, BlePenConst.UUID_PAN_BATTERY_LEVEL);
                    joinReadCharaCommand(BlePenConst.UUID_SERVICE_DEVICE_INFO, BlePenConst.UUID_CHARA_R_HARDWARE_VERSION);
                    joinReadCharaCommand(BlePenConst.UUID_SERVICE_DEVICE_INFO, BlePenConst.UUID_CHARA_R_FIRMWARE_VERSION);
                    mCallbacks.onDeviceConnected(gatt.getDevice());
                    mIsTimeoutRunning = false;
//                    if (mTimeoutThread != null) {
//                        mTimeoutThread.interrupt();
//                        mTimeoutThread = null;
//                    }
                    if (mTimer != null) mTimer.cancel();
                } else {
                    LogUtil.e(TAG, "设备服务查找失败！");
                    //服务没找到再次查找，因为部分手机连接后发现服务需要等待时间比较长
                    if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING && mFindServicesCount < SEARCH_SERVICE_MAX) {
                        mFindServicesCount++;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mFindServicesCount > 2) {
                                    refreshDeviceCache(gatt);
                                }
                                gatt.discoverServices();
                                LogUtil.d(TAG, "重新查找服务 count = " + mFindServicesCount);
                            }
                        }, DelaySearchServiceTime);
                        return;
                    }
                    mFindServicesCount = 0;
                    doDisconnect(false);
                }
            } else {
                LogUtil.e(TAG, "服务查找错误 status = " + status);
                onError(gatt.getDevice(), ERROR_DISCOVERY_SERVICE, status);
            }

        }

        @Override
        public final void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                UUID uuid = characteristic.getUuid();
                byte[] data = characteristic.getValue();

                LogUtil.i(TAG, "onCharacteristicRead : uuid = " + uuid);

                if (BlePenConst.UUID_PAN_BATTERY_LEVEL.equals(uuid)) {
                    onPenBatteryStateChanged(data);
                } else if (BleConst.UUID_CHARA_R_N_BATTERY.equals(uuid)) {
                    onBatteryStateChanged(data);
                } else if (BleConst.UUID_CHARA_R_N_DEVICE_STATE_CHANGE.equals(uuid)) {
                    onSensorStateChange(uuid, data);
                } else if (BlePenConst.UUID_CHARA_R_FIRMWARE_VERSION.equals(uuid)) {
                    onPenFirmwareVersionChanged(data);
                } else if (BleConst.UUID_CHARA_R_FIRMWARE_VERSION.equals(uuid)) {
                    onFirmwareVersionChanged(data);
                } else {
                    final Intent intent = new Intent(BleConst.NOTIFICATION_NORMAL_READ);
                    intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
                    intent.putExtra(BleConst.EXTRA_DATA, data);
                    intent.putExtra(BleConst.EXTRA_COMMAND_UUID, uuid);
                    intent.putExtra(BleConst.EXTRA_COMMAND_RESULT, true);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    // This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
                    LogUtil.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                    onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            } else {
                LogUtil.e(TAG, "onCharacteristicRead error " + status);
                onError(gatt.getDevice(), ERROR_READ_CHARACTERISTIC, status);
            }
            synchronized (mSendLock) {
                LogUtil.d(TAG, "命令发送结束");
                isRequesting = true;
                mHandler.post(mProcessNextTask);
            }
        }

        @Override
        public final void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            UUID uuid = characteristic.getUuid();
            byte[] cmd = characteristic.getValue();
            boolean isSuccess = false;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // The value has been written. Notify the profile and proceed with the initialization queue.
                isSuccess = true;

                LogUtil.i(TAG, "onCharacteristicWrite : address = " + gatt.getDevice().getAddress() + " cmd=" + DataUtil.convertByteToHexString(cmd));

            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    // This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
                    LogUtil.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                    onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            } else {
                LogUtil.e(TAG, "onCharacteristicWrite error " + status);
                onError(gatt.getDevice(), ERROR_WRITE_CHARACTERISTIC, status);
            }

            //写入通知
            final Intent intent = new Intent(BleConst.NOTIFICATION_NORMAL_WRITE);
            intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(BleConst.EXTRA_COMMAND_UUID, uuid);
            intent.putExtra(BleConst.EXTRA_COMMAND_DATA, cmd);
            intent.putExtra(BleConst.EXTRA_COMMAND_RESULT, isSuccess);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            synchronized (mSendLock) {
                LogUtil.d(TAG, "命令发送结束");
                isRequesting = true;
                mHandler.post(mProcessNextTask);
            }
        }

        @Override
        public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            LogUtil.d(TAG, "onDescriptorRead " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // The value has been read. Notify the profile and proceed with the initialization queue.


            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    // This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
                    LogUtil.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                    onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            } else {
                LogUtil.e(TAG, "onDescriptorRead error " + status);
                onError(gatt.getDevice(), ERROR_READ_DESCRIPTOR, status);
            }
            synchronized (mSendLock) {
                LogUtil.d(TAG, "命令发送结束");
                isRequesting = true;
                mHandler.post(mProcessNextTask);
            }

        }

        @Override
        public final void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            boolean isSuccess = false;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // The value has been written. Notify the profile and proceed with the initialization queue.

                isSuccess = true;
                LogUtil.d(TAG, "onDescriptorWrite : address = " + gatt.getDevice().getAddress());
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    // This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
                    LogUtil.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
                    onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            } else {
                LogUtil.e(TAG, "onDescriptorWrite error " + status);
                onError(gatt.getDevice(), ERROR_WRITE_DESCRIPTOR, status);
            }


            UUID uuid = descriptor.getCharacteristic().getUuid();
            byte[] cmd = descriptor.getCharacteristic().getValue();
            //写入通知
            final Intent intent = new Intent(BleConst.NOTIFICATION_NORMAL_NOTIFICATION);
            intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(BleConst.EXTRA_COMMAND_UUID, uuid);
            intent.putExtra(BleConst.EXTRA_COMMAND_DATA, cmd);
            intent.putExtra(BleConst.EXTRA_COMMAND_RESULT, isSuccess);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            synchronized (mSendLock) {
                LogUtil.d(TAG, "命令发送结束");
                isRequesting = true;
                mHandler.post(mProcessNextTask);
            }
        }

        @Override
        public final void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

            UUID uuid = characteristic.getUuid();
            byte[] data = characteristic.getValue();
            if (uuid.equals(BlePenConst.UUID_PAN_BATTERY_LEVEL)) {
                onPenBatteryStateChanged(data);
            }
            else if (uuid.equals(BlePenConst.UUID_PAN_TIME_LIST_UPDATE) || uuid.equals(BlePenConst.UUID_PAN_TIME_LIST)) {
                //写入通知
                final Intent intent = new Intent(BleConst.NOTIFICATION_NORMAL_NOTIFICATION);
                intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
                intent.putExtra(BleConst.EXTRA_COMMAND_UUID, uuid);
                intent.putExtra(BleConst.EXTRA_COMMAND_DATA, data);
                intent.putExtra(BleConst.EXTRA_COMMAND_RESULT, true);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
            else if (uuid.equals(BleConst.UUID_CHARA_R_N_BATTERY)) {
                // 电池电量
                onBatteryStateChanged(data);
            } else if (uuid.equals(BleConst.UUID_CHARA_R_N_ALGORITHM)) {
                // 算法数据
                final Intent intent = new Intent(BleConst.NOTIFICATION_ALGORITHM);
                intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
                intent.putExtra(BleConst.EXTRA_DATA, data);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                if (mAlgorithmCallback != null) {
                    mAlgorithmCallback.onDataUpdated(mBluetoothDevice, data);
                }

            } else if (BleConst.UUID_CHARA_R_N_DEVICE_STATE_CHANGE.equals(uuid)) {
                onSensorStateChange(uuid, data);
            } else if (uuid.equals(BleConst.UUID_CHARA_R_N_ORIGINAL)) {
                final Intent intent = new Intent(BleConst.NOTIFICATION_ORIGINAL);
                intent.putExtra(BleConst.EXTRA_DEVICE, mBluetoothDevice);
                intent.putExtra(BleConst.EXTRA_DATA, data);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }

    }
}
