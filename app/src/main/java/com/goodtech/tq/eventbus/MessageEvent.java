package com.goodtech.tq.eventbus;

/**
 * com.goodtech.tq.eventbus
 */
public class MessageEvent {

    protected boolean location;
    //  点击城市的index
    protected int cityIndex = -1;

    protected int fetchCId;

    protected boolean addCity;

    public MessageEvent() {

    }

    public MessageEvent setLocation(boolean location) {
        this.location = location;
        return this;
    }

    public boolean isSuccessLocation() {
        return location;
    }

    public MessageEvent setCityIndex(int cityIndex) {
        this.cityIndex = cityIndex;
        return this;
    }

    public int showIndex() {
        return cityIndex;
    }

    public MessageEvent setFetchCId(int fetchCId) {
        this.fetchCId = fetchCId;
        return this;
    }

    public int getFetchCId() {
        return fetchCId;
    }

    public MessageEvent addCity(boolean addCity) {
        this.addCity = addCity;
        return this;
    }

    public boolean isAddCity() {
        return addCity;
    }
}
