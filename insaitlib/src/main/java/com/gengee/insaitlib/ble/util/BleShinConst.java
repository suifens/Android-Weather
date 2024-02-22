package com.gengee.insaitlib.ble.util;

import java.util.UUID;

import static java.util.UUID.fromString;

public class BleShinConst {

    public static final String UUID_BASE_HEAD = "494E";
    public static final String UUID_BASE_TAIL = "-5341-4954-5332-50494C414443";

    //设备信息服务
    public static final UUID UUID_SERVICE_DEVICE_INFO = fromString(UUID_BASE_HEAD + "180A" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_R_FIRMWARE_VERSION = fromString(UUID_BASE_HEAD + "2A26" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_R_HARDWARE_VERSION = fromString(UUID_BASE_HEAD + "2A27" + UUID_BASE_TAIL);

    //
    public static final UUID UUID_SERVICE_SENSOR_DATA = fromString(UUID_BASE_HEAD + "AB80" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_W_SET_DEVICE = fromString(UUID_BASE_HEAD + "AB81" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_W_CHANGE_MODEL = fromString(UUID_BASE_HEAD + "AB82" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_R_N_DEVICE_STATE_CHANGE = fromString(UUID_BASE_HEAD + "AB83" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_R_N_BATTERY = fromString(UUID_BASE_HEAD + "AB84" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_R_N_ALGORITHM = fromString(UUID_BASE_HEAD + "AB86" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_R_SENSOR_USE_INFO =  fromString(UUID_BASE_HEAD + "AB8C" + UUID_BASE_TAIL);

    
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
