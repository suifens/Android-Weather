package com.goodtech.tq.common.bus.event;

import com.goodtech.tq.common.bus.BusEvent;

public class DPStartEvent extends BusEvent {
    public boolean isSuccess;

    public DPStartEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
