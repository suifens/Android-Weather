package com.goodtech.tq.eventbus;

/**
 * com.goodtech.tq.eventbus
 */
public class CityEvent {

    protected boolean location;
    //  点击城市的index
    protected int cityIndex = -1;

    protected boolean addCity;

    protected boolean needReload;

    public CityEvent() {

    }

    public CityEvent setLocation(boolean location) {
        this.location = location;
        return this;
    }

    public boolean isSuccessLocation() {
        return location;
    }

    public CityEvent setCityIndex(int cityIndex) {
        this.cityIndex = cityIndex;
        return this;
    }

    public int showIndex() {
        return cityIndex;
    }

    public CityEvent addCity(boolean addCity) {
        this.addCity = addCity;
        return this;
    }

    public boolean isAddCity() {
        return addCity;
    }

    public CityEvent needReload(boolean needReload) {
        this.needReload = needReload;
        return this;
    }

    public boolean isNeedReload() {
        return needReload;
    }
}
