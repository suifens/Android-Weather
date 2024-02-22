package com.gengee.insaitlib.ble.core;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import com.gengee.insaitlib.ble.util.BleConst;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class FootBallProfile {
    public static boolean matchDevice(final BluetoothGatt gatt) {
        final BluetoothGattService service = gatt.getService(BleConst.UUID_SERVICE_SENSOR_DATA);
//        return service != null && service.getCharacteristic(BleConst.UUID_CHARA_W_SET_DEVICE) != null;
        return true;
    }
}
