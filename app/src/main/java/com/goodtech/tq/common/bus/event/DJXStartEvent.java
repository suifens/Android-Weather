package com.goodtech.tq.common.bus.event;

import com.goodtech.tq.common.bus.BusEvent;

public class DJXStartEvent extends BusEvent {
    public boolean isSuccess;

    public DJXStartEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
