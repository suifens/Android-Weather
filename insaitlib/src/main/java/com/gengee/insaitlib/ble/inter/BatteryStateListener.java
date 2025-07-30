package com.gengee.insaitlib.ble.inter;

import com.gengee.insaitlib.ble.model.BatteryInfo;

/**
	 * 电池状态监听器
	 * 
	 * @author GenGee
	 * 
	 */
	public interface BatteryStateListener {

		/**
		 * 当电池电量变化时会触发这个方法
		 *
		 * @param battery
		 */
		void onBatteryStateChanged(BatteryInfo battery);
	}