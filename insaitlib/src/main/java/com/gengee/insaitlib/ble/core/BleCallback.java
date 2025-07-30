package com.gengee.insaitlib.ble.core;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * 回调接口
 *
 */
interface BleCallback {

	void onConnectionEstablished(BluetoothGatt gatt);

	void onDisconnect(BluetoothGatt gatt);

	void onServiceDiscovered(BluetoothGatt gatt,int status);

	void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

	void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status);

	void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status);

	void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,int status);

	void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,int status);

}
