package com.gengee.insaitlib.ble.inter;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public interface BleStateListener {
    /**
     * 蓝牙被打开
     */
    void onBleStateOn();

    /**
     * 蓝牙被关闭
     */
    void onBleStateOff();
}
