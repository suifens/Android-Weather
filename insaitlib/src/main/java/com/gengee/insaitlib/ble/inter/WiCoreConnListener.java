package com.gengee.insaitlib.ble.inter;

/**
 * 连接监听接口
 *
 * 
 */
public interface WiCoreConnListener {

	void onConnected(String address);

	void onDisconnected(String address);
}