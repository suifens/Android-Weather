package com.gengee.insaitlib.ble.util;

import java.util.UUID;

import static java.util.UUID.fromString;

public class BleConst {

    public static final String UUID_BASE_HEAD = "494E";
    public static final String UUID_BASE_TAIL = "-5341-4954-5332-50494C414443";

    //设备信息服务
    //设备信息服务 Service & Characteristic (Read Only)
    public static final UUID UUID_SERVICE_DEVICE_INFO = fromString(UUID_BASE_HEAD + "180A" + UUID_BASE_TAIL);
    //  System ID  3byte
    public static final UUID UUID_CHARA_R_SYSTEM_ID = fromString(UUID_BASE_HEAD + "2A23" + UUID_BASE_TAIL);
    //  Module Number   4byte
    public static final UUID UUID_CHARA_R_MODULE_NUMBER = fromString(UUID_BASE_HEAD + "2A24" + UUID_BASE_TAIL);
    //  Serial Number   6byte
    public static final UUID UUID_CHARA_R_SERIAL_NUMBER = fromString(UUID_BASE_HEAD + "2A25" + UUID_BASE_TAIL);
    //  Firmware Version 5byte
    public static final UUID UUID_CHARA_R_FIRMWARE_VERSION = fromString(UUID_BASE_HEAD + "2A26" + UUID_BASE_TAIL);
    //  Hardware Version 8byte
    public static final UUID UUID_CHARA_R_HARDWARE_VERSION = fromString(UUID_BASE_HEAD + "2A27" + UUID_BASE_TAIL);
    //  Software Version 16byte
    public static final UUID UUID_CHARA_R_SOFTWARE_VERSION = fromString(UUID_BASE_HEAD + "2A28" + UUID_BASE_TAIL);
    //  Manufacturer 6byte
    public static final UUID UUID_CHARA_R_MANUFACTURER = fromString(UUID_BASE_HEAD + "2A29" + UUID_BASE_TAIL);

    //
    public static final UUID UUID_SERVICE_SENSOR_DATA = fromString(UUID_BASE_HEAD + "AB80" + UUID_BASE_TAIL);
    //  配置设备 Write  size:0~20
    public static final UUID UUID_CHARA_W_SET_DEVICE = fromString(UUID_BASE_HEAD + "AB81" + UUID_BASE_TAIL);
    //  模式切换 Write  size:9
    public static final UUID UUID_CHARA_W_CHANGE_MODEL = fromString(UUID_BASE_HEAD + "AB82" + UUID_BASE_TAIL);
    //  设备状态信息 Read size: 1
    public static final UUID UUID_CHARA_R_N_DEVICE_STATE_CHANGE = fromString(UUID_BASE_HEAD + "AB83" + UUID_BASE_TAIL);
    //  电池电量信息 R/N  size: 1
    public static final UUID UUID_CHARA_R_N_BATTERY = fromString(UUID_BASE_HEAD + "AB84" + UUID_BASE_TAIL);
    //  原始传感器数据 R/N size: 20
    public static final UUID UUID_CHARA_R_N_ORIGINAL = fromString(UUID_BASE_HEAD + "AB85" + UUID_BASE_TAIL);
    //  算法数据 R/N    size: 20
    public static final UUID UUID_CHARA_R_N_ALGORITHM = fromString(UUID_BASE_HEAD + "AB86" + UUID_BASE_TAIL);
    //  BLE 频率 Read     size: 1
    public static final UUID UUID_CHARA_R_BLE_FREQUENCY = fromString(UUID_BASE_HEAD + "AB87" + UUID_BASE_TAIL);
    //  传感器校准值 R    size: 19
    public static final UUID UUID_CHARA_R_CALIBRATION_VALUE = fromString(UUID_BASE_HEAD + "AB88" + UUID_BASE_TAIL);
    //  BLE 前缀名 R     size: 0~20
    public static final UUID UUID_CHARA_R_BLE_PREFIX = fromString(UUID_BASE_HEAD + "AB89" + UUID_BASE_TAIL);
    //  BLE 后缀名 R     size: 0~20
    public static final UUID UUID_CHARA_R_BLE_SUFFIX = fromString(UUID_BASE_HEAD + "AB8A" + UUID_BASE_TAIL);
    //  训练数据 R       size: 20
    public static final UUID UUID_CHARA_R_TRAIN_DATA = fromString(UUID_BASE_HEAD + "AB8B" + UUID_BASE_TAIL);
    //  产品使用信息 R    size: 20
    public static final UUID UUID_CHARA_R_SENSOR_USE_INFO = fromString(UUID_BASE_HEAD + "AB8C" + UUID_BASE_TAIL);
    //  指令接收
    public static final UUID UUID_CHARA_R_N_SENSOR_DATA = fromString(UUID_BASE_HEAD + "AB8F" + UUID_BASE_TAIL);
    //  固件更新
    public static final UUID UUID_SERVICE_SENSOR_UPGRADE = fromString("00001530-1212-efde-1523-785feabcd123");
    //  Control Point
    public static final UUID UUID_SERVICE_W_TO_DFU = fromString("00001531-1212-efde-1523-785feabcd123");

    // 芯片数据读写模式command
    public static final byte SENSOR_TYPE_DISCARD = 0x01;
    public static final byte SENSOR_TYPE_ALGORITHM = 0x04;
    public static final byte SENSOR_TYPE_CALIBRATION = 0x06;


    public static final byte ALGORITHM_STEPS = 0x01;
    public static final byte ALGORITHM_TIP_TAP = 0x02;
    public static final byte ALGORITHM_ROLL_TAP = 0x03;
    public static final byte ALGORITHM_PUSH_PULL = 0x04;
    public static final byte ALGORITHM_OUTINSIDE = 0x05;
    public static final byte ALGORITHM_HALF_SPIN = 0x06;
    public static final byte ALGORITHM_JUGGLE = 0x07;
    public static final byte ALGORITHM_STALL = 0x08;
    public static final byte ALGORITHM_BOTH_FEET_PUSH_PULL = 0x09;
    public static final byte ALGORITHM_ONE_FEET_PUSH_PULL = 0x0A;
    public static final byte ALGORITHM_V_PUSH_PULL = 0x0B;
    public static final byte ALGORITHM_TIP_TAP_PULL = 0x0C;
    public static final byte ALGORITHM_BACK_CHOP = 0x0D;
    public static final byte ALGORITHM_PULL_BACK_TIP_TAP = 0x0E;
    public static final byte ALGORITHM_PULL_STALL = 0x0F;
    public static final byte ALGORITHM_V_BACK_PUSH_PULL = 0x10;
    public static final byte ALGORITHM_SIDE_STEPS = 0x11;
    public static final byte ALGORITHM_INSTEP_PUSH_PULL = 0x12;
    public static final byte ALGORITHM_BOTH_FEET_INSTEP_PUSH_PULL = 0x13;
    public static final byte ALGORITHM_V_INSTEP_PUSH_PULL = 0x14;




    public static final byte SENSOR_ALGORITHM_SUB_KICK = 0x54;
    public static final byte SENSOR_ALGORITHM_SUB_STOP = (byte) 0xff;


    public static final byte SENSOR_CALIBRATION_START = (byte) 0x01;
    public static final byte SENSOR_CALIBRATION_VALIDATION = (byte) 0x02;
    public static final byte SENSOR_CALIBRATION_STOP = (byte) 0xFF;


    //配置设备信息
    public static final byte SENSOR_CONF_LAST_NAME = 0X04;
    public static final byte UPGRADE_TO_DFU = 0X01;


    public static final byte DEVICE_STATE_IDLE = (byte) 0x00;
    public static final byte DEVICE_STATE_Sensor_Calibration_Validation = (byte) 0x07;

    public static final byte DEVICE_STATE_Motion_Detect = (byte) 0x04;

    public static final byte DEVICE_STATE_JUGGLE = (byte) 0x01;
    public static final byte DEVICE_STATE_PULL_BACK = (byte) 0x02;
    public static final byte DEVICE_STATE_TIP_TAP = (byte) 0x03;
    public static final byte DEVICE_STATE_KICK = (byte) 0x04;
//    public static final byte DEVICE_STATE_FREE_STYLE_JUGGLE = (byte) 0x08;


    // 运动数据服务
    public static final UUID UUID_SRV_MOVEMENT = fromString(UUID_BASE_HEAD + "AA80" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_MOVEMENT_DATA = fromString(UUID_BASE_HEAD + "AA81" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_MOVEMENT_CONF = fromString(UUID_BASE_HEAD + "AA82" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_MOVEMENT_OPMODE = fromString(UUID_BASE_HEAD + "AA83" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_MOVEMENT_FREQ = fromString(UUID_BASE_HEAD + "AA84" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_MOVEMENT_BATTERY = fromString(UUID_BASE_HEAD + "AA85" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_MOVEMENT_ALGDATA = fromString(UUID_BASE_HEAD + "AA86" + UUID_BASE_TAIL);

    // 连接控制服务
    //public static final UUID	UUID_SRV_CONNECTION_CTRL  	 = fromString("f000ccc0-0451-4000-b000-000000000000");
    //public static final UUID	UUID_CHARA_REQ_CONN_PARAMS 	 = fromString("F000CCC2-0451-4000-B000-000000000000");
    public static final UUID UUID_SRV_CONNECTION_CTRL = fromString(UUID_BASE_HEAD + "CCC0" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_CONN_PARAMS = fromString(UUID_BASE_HEAD + "CCC1" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_REQ_CONN_PARAMS = fromString(UUID_BASE_HEAD + "CCC2" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_DISCONN_REQ = fromString(UUID_BASE_HEAD + "CCC3" + UUID_BASE_TAIL);

    // OAD服务
    //public static final UUID	UUID_SRV_OAD 				 = fromString("f000ffc0-0451-4000-b000-000000000000");
    //public static final UUID	UUID_CHARA_IMAGE_IDEN 		 = fromString("f000ffc1-0451-4000-b000-000000000000");
    //public static final UUID	UUID_CHARA_IMAGE_BLOCK 		 = fromString("f000ffc2-0451-4000-b000-000000000000");
    public static final UUID UUID_SRV_OAD = fromString(UUID_BASE_HEAD + "FFC0" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_IMAGE_IDEN = fromString(UUID_BASE_HEAD + "FFC1" + UUID_BASE_TAIL);
    public static final UUID UUID_CHARA_IMAGE_BLOCK = fromString(UUID_BASE_HEAD + "FFC2" + UUID_BASE_TAIL);

    // 固件修改
    public static final UUID UUID_CHARA_FIRMWARE_REVISION = fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID[] UUIDS_MOVEMENT_SRV = new UUID[]{
            UUID_SRV_MOVEMENT,
    };

    //设备信息
    public static final UUID[] UUIDS_MOVEMENT_CHAR = new UUID[]{
            UUID_CHARA_MOVEMENT_DATA,
            UUID_CHARA_MOVEMENT_CONF,
            UUID_CHARA_MOVEMENT_OPMODE,
            UUID_CHARA_MOVEMENT_FREQ,
            UUID_CHARA_MOVEMENT_BATTERY
    };
    //系统ID
    public static final UUID[] UUIDS_CCC = new UUID[]{
            UUID_CHARA_MOVEMENT_DATA,};


    private static String UUID_SIG_BASE_HEAD = "0000";
    private static String UUID_SIG_BASE_TAIL = "-0000-1000-8000-00805F9B34FB";
    // UUID_CHARA_MOVEMENT_OPMODE,
    // UUID_CHARA_MOVEMENT_BATTERY};


    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    public static final String EXTRA_DEVICE_TYPE = "EXTRA_DEVICE_TYPE";
    public static final String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";
    public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
    public static final String EXTRA_COMMAND_UUID = "EXTRA_COMMAND_UUID";//发送指令id
    public static final String EXTRA_COMMAND_DATA = "EXTRA_COMMAND_DATA";//发送指令数据
    public static final String EXTRA_COMMAND_RESULT = "EXTRA_COMMAND_RESULT";//发送结果
    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String EXTRA_SENSOR_STATE = "EXTRA_SENSOR_STATE";
    public static final String EXTRA_SENSOR_TYPE = "EXTRA_SENSOR_TYPE";

    public static final String NOTIFICATION_BATTERY = "NOTIFICATION_BATTERY_";//电池电量
    public static final String NOTIFICATION_ALGORITHM = "NOTIFICATION_ALGORITHM";//算法数据
    public static final String NOTIFICATION_ORIGINAL = "NOTIFICATION_ORIGINAL";//算法数据
    public static final String NOTIFICATION_DEVICE_STATE = "NOTIFICATION_DEVICE_STATE";//设备状态变化
    public static final String NOTIFICATION_CONNECT_CHANGE = "NOTIFICATION_CONNECT_CHANGE";//设备连接状态
    public static final String NOTIFICATION_NORMAL_READ = "NOTIFICATION_NORMAL_READ";//设备特征读取
    public static final String NOTIFICATION_NORMAL_WRITE = "NOTIFICATION_NORMAL_WRITE";//设备特征写入
    public static final String NOTIFICATION_NORMAL_NOTIFICATION = "NOTIFICATION_NORMAL_NOTIFICATION";//设备广播设置

    public static final byte CONNECT_CONNECTING = 0X01;
    public static final byte CONNECT_CONNECTED = 0X02;
    public static final byte CONNECT_DISCONNECTING = 0X03;
    public static final byte CONNECT_DISCONNECTED = 0X04;
    public static final byte CONNECT_FAIL = 0X05;

    public final static String[] PREFIX_LIST = {"G-INSAIT", "G-4-INSAIT", "G-5-INSAIT", "G-SPTDC"};
    public final static String[] PREFIX_LIST_B = {"G-B5-INSAIT", "G-B7-INSAIT"};
    public static final String[] PREFIX_LIST_SHIN = {"R JOY", "L JOY"};
    public static final String PREFIX_SHIN_L = "L JOY ";
    public static final String PREFIX_SHIN_R = "R JOY ";

    public static final String PREFIX_PAN = "INSAIT-";
}
