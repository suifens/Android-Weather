package com.gengee.insaitlib.ble.dic;

import androidx.annotation.Keep;

@Keep
public enum ScanErrorState {
    None,   //  正常
    NotSupportBle, //   不支持蓝牙
    BleClose,    //  蓝牙关闭
    Permission, //  权限问题
    Timeout,    //超时
}
