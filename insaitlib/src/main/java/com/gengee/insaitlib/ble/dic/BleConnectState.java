package com.gengee.insaitlib.ble.dic;

import androidx.annotation.Keep;

@Keep
public enum BleConnectState {
    Connecting, //  连接中
    Timeout,    //  超时
    Fail,   //  失败
    Success, //  成功

    Disconnected // 断开连接
}
