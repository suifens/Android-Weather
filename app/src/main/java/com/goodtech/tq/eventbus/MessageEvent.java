package com.goodtech.tq.eventbus;

/**
 * com.goodtech.tq.eventbus
 */
public class MessageEvent {

    protected boolean location;
    protected String fetchCId;

    protected boolean needReload;

    public MessageEvent() {

    }

    public MessageEvent setLocation(boolean location) {
        this.location = location;
        return this;
    }

    public boolean isSuccessLocation() {
        return location;
    }

    public MessageEvent setFetchCId(String fetchCId) {
        this.fetchCId = fetchCId;
        return this;
    }

    public String getFetchCId() {
        if (fetchCId == null) {
            return "";
        }
        return fetchCId;
    }

    public MessageEvent needReload(boolean needReload) {
        this.needReload = needReload;
        return this;
    }

    public boolean isNeedReload() {
        return needReload;
    }
}
