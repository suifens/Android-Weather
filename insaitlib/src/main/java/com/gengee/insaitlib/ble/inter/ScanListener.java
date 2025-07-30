package com.gengee.insaitlib.ble.inter;

import android.bluetooth.BluetoothDevice;

import com.gengee.insaitlib.ble.dic.BleDeviceType;

/**
 * 扫描监听接口
 *
 * 
 */
public interface ScanListener {

	/**
	 * @param type
	 * @param device
	 * @param rssi 信号强弱
	 */
	void onDeviceFound(BleDeviceType type, BluetoothDevice device, int rssi, byte[] scanRecord);
}
