package com.gengee.insaitlib.ble.inter;

import java.util.UUID;

/**
 * 传感器数据监听接口。
 */
public interface SensorDataListener {
    
    /**
     * 设置写命令回调
     *
     * @param uuid
     * @param command
     * @param isSuccess 是否发送成功
     */
    void onWriteCommandResult(UUID uuid, byte[] command, boolean isSuccess);
    
    /**
     * 读数据命令回调
     *
     * @param uuid
     * @param data
     * @param isSuccess 是否发送成功
     */
    void onReadCommandResult(UUID uuid, byte[] data, boolean isSuccess);
    
    /**
     * 设置广播命令回调
     *
     * @param uuid
     * @param data
     * @param isSuccess 是否发送成功
     */
    void onNotifyCommandResult(UUID uuid, byte[] data, boolean isSuccess);
    
    /**
     * 设备状态改变
     * @param uuid
     * @param state
     * @param type
     */
    void onDeviceStateChange(UUID uuid,int state,int type);
    
    void onDataUpdated(byte[] data);
}
