package com.gengee.insaitlib.ble.util;

import static java.util.UUID.fromString;

import java.util.UUID;

public class BlePenConst {

    public static final String UUID_BASE_HEAD = "4331";
    public static final String UUID_BASE_TAIL = "-496E-7361-6974-47656E676565";

    //设备信息服务
    public static final UUID UUID_SERVICE_DEVICE_INFO = fromString("0000180A-0000-1000-8000-00805F9B34FB");
    public static final UUID UUID_MODEL_NUMBER = fromString(UUID_BASE_HEAD + "2A24" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_R_FIRMWARE_VERSION = fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public static final UUID UUID_CHARA_R_HARDWARE_VERSION = fromString("00002A27-0000-1000-8000-00805F9B34FB");

    //
    public static final UUID UUID_SERVICE_PEN = fromString(UUID_BASE_HEAD + "FC40" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_DEVICE_ID = fromString(UUID_BASE_HEAD + "FC41" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_TIME_LIST = fromString(UUID_BASE_HEAD + "FC42" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_TIME_LIST_UPDATE = fromString(UUID_BASE_HEAD + "FC43" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_CLEAR_TIME_LIST = fromString(UUID_BASE_HEAD + "FC44" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_AUDIO = fromString(UUID_BASE_HEAD + "FC45" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_AUDIO_UPDATE = fromString(UUID_BASE_HEAD + "FC46" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_AUDIO_CLEAR =  fromString(UUID_BASE_HEAD + "FC47" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_BATTERY_LEVEL =  fromString(UUID_BASE_HEAD + "FC48" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_CORDER_STATE =  fromString(UUID_BASE_HEAD + "FC49" + UUID_BASE_TAIL);
    public static final UUID UUID_PAN_DEVICE_NAME =  fromString(UUID_BASE_HEAD + "FC4A" + UUID_BASE_TAIL);

    
    //配置设备信息
    public static final byte SENSOR_CONF_LAST_NAME = 0x05;
    public static final byte SENSOR_CONF_SET_PAIR = 0x04;
    public static final byte SENSOR_CONF_CLEAR_PAIR = 0x06;
    public static final byte SENSOR_CONF_BIND = 0x08;
    public static final byte SENSOR_CONF_CLEAN_STAT = 0x09;
    public static final byte SENSOR_CONF_RESULT = 0x0a;
    public static final byte SENSOR_CONF_USER_ID = 0x0d;
    public static final byte SENSOR_CONF_STEP_FILTER = 0x0C;

    // 固件修改
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = fromString("00002902-0000-1000-8000-00805f9b34fb");
   

    // 算法类型
    public static final byte MDA_TYPE_DRIBBLE = 0x01;
    public static final byte MDA_TYPE_CIRCLE = 0x02;
    

}
