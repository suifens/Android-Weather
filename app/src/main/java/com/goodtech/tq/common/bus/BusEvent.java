package com.goodtech.tq.common.bus;

import androidx.annotation.NonNull;

import java.util.Locale;

public class BusEvent {
    private boolean mIsMainThread = true;//消息接收是否在主线程，默认true
    private long mSendTime = 0;
    private long mReceiveTime = 0;


    void updateRcvTime() {
        if (mReceiveTime == 0) {
            mReceiveTime = System.currentTimeMillis();
        }
    }

    public boolean isMainThread() {
        return mIsMainThread;
    }

    /**
     * 发送事件，在主线程接收
     */
    public void send() {
        mIsMainThread = true;
        mSendTime = System.currentTimeMillis();
        Bus.getInstance().sendEvent(this);
    }

    /**
     * 发送事件，在主线程接收
     *
     * @param delay 延迟事件，单位：毫秒
     */
    public void send(long delay) {
        mIsMainThread = true;
        mSendTime = System.currentTimeMillis();
        Bus.getInstance().sendEvent(this, delay);
    }

    /**
     * 发送事件，在异步线程接收
     */
    public void sendOnThread() {
        mIsMainThread = false;
        mSendTime = System.currentTimeMillis();
        Bus.getInstance().sendEvent(this);
    }

    /**
     * 发送事件，在异步线程接收
     *
     * @param delay 延迟事件，单位：毫秒
     */
    public void sendOnThread(long delay) {
        mIsMainThread = false;
        mSendTime = System.currentTimeMillis();
        Bus.getInstance().sendEvent(this, delay);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "(:IN-QUEUE %d)", mReceiveTime - mSendTime);
    }

}
