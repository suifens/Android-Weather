package com.goodtech.tq.common.bus;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Create by hanweiwei on 2020-03-31.
 */
public class Bus {
    private static final String TAG = "DPBus";
    private static final int MSG_ID_EVENT = 6688;
    private static volatile Bus sInstance = null;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mTH;
    private Handler mH;
    private final Collection<IBusListener> mStack = Collections.asLifoQueue(new LinkedBlockingDeque<IBusListener>());

    public static Bus getInstance() {
        if (sInstance == null) {
            synchronized (Bus.class) {
                if (sInstance == null) {
                    sInstance = new Bus();
                }
            }
        }
        return sInstance;
    }

    private Bus() {
        restart();
    }

    public synchronized void restart() {
        if (mH == null || mTH == null) {
            mTH = new HandlerThread("DPBus", Thread.NORM_PRIORITY);
            mTH.start();
            mH = new Handler(mTH.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (isEventMessage(msg)) {
                        onHandleEvent((BusEvent) msg.obj);
                    }
                }
            };
        }
    }

    private boolean isEventMessage(final Message msg) {
        return msg.what == MSG_ID_EVENT && msg.obj instanceof BusEvent;
    }

    public void addListener(IBusListener l) {
        if (!mStack.contains(l)) {
            mStack.add(l);
        }
    }

    public void removeListener(IBusListener l) {
        try {
            mStack.remove(l);
        } catch (Throwable ignore) {
        }
    }

    public void clear() {
        try {
            if (!mStack.isEmpty()) {
                mStack.clear();
            }
        } catch (Throwable ignore) {
        }
    }

    private void onHandleEvent(final BusEvent event) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (IBusListener x : mStack) {
                    try {
                        event.updateRcvTime();
                        x.onBusEvent(event);
                    } catch (Throwable e) {
                        Log.w(TAG, "dpbus handle error: ", e);
                    }
                }
            }
        };

        if (event.isMainThread()) {
            mMainHandler.post(task);
        } else {
            task.run();
        }
    }

    public void sendEvent(BusEvent event) {
        Message m = Message.obtain();
        m.what = MSG_ID_EVENT;
        m.obj = event;
        mH.sendMessage(m);
    }

    public void sendEvent(BusEvent event, long delay) {
        Message m = Message.obtain();
        m.what = MSG_ID_EVENT;
        m.obj = event;
        mH.sendMessageDelayed(m, delay);
    }
}
