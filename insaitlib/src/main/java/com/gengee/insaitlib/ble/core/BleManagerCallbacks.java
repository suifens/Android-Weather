package com.gengee.insaitlib.ble.core;

import android.bluetooth.BluetoothDevice;

public interface BleManagerCallbacks {

	/**
	 * Called when the Android device started connecting to given device.
	 * The {@link #onDeviceConnected(BluetoothDevice)} will be called when the device is connected,
	 * or {@link #onError(BluetoothDevice, String, int)} in case of error.
	 * @param device the device that got connected
	 */
	void onDeviceConnecting(final BluetoothDevice device);

	/**
	 * Called when the device has been connected. This does not mean that the application may start communication.
	 * A service discovery will be handled automatically after this call. Service discovery
	 * may ends up with calling {@link #onDeviceReady(BluetoothDevice)}
	 * or {@link #onDeviceNotSupported(BluetoothDevice)} if required services have not been found.
	 * @param device target device
	 */
	void onDeviceConnected(final BluetoothDevice device);

	/**
	 * Method called when all initialization requests has been completed.
	 * @param device target device
	 */
	void onDeviceReady(final BluetoothDevice device);


	/**
	 * Called when user initialized disconnection.
	 * @param device target device
	 */
	void onDeviceDisconnecting(final BluetoothDevice device);

	/**
	 * Called when the device has disconnected
	 * @param device the device that got disconnected
	 */
	void onDeviceDisconnected(final BluetoothDevice device,boolean isUserDisconnect);

	/**
	 * This callback is invoked when the Ble Manager lost connection to a device that has been connected with autoConnect option.
	 * Otherwise a {@link #onDeviceDisconnected(BluetoothDevice,boolean)} method will be called on such event.
	 * @param device target device
	 */
	void onLinklossOccur(final BluetoothDevice device);
	

	/**
	 * Called when a BLE error has occurred
	 *
	 * @param device target device
	 * @param message the error message
	 * @param errorCode the error code
	 */
	void onError(final BluetoothDevice device, final String message, final int errorCode);
	
}
