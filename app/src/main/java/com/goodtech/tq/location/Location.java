package com.goodtech.tq.location;

import java.io.Serializable;

/**
 * com.goodtech.tq.location
 */
public class Location implements Serializable {
    // 排序
    private int listNum;
    // 是否定位
    private boolean location;
    // 纬度
    private double latitude;
    // 经度
    private double longitude;
    // 获取省份
    private String province;
    // 城市编码
    private String cityCode;
    // 城市
    private String city;
    // 区，县
    private String district;
    // 街道
    private String street;
    //  地址信息
    private String addrStr;

    public int getListNum() {
        return listNum;
    }

    public void setListNum(int listNum) {
        this.listNum = listNum;
    }

    public boolean isLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAddrStr() {
        return addrStr;
    }

    public void setAddrStr(String addrStr) {
        this.addrStr = addrStr;
    }

    public static Location location(android.location.Location rawLocation) {
        Location location = new Location();
        if (rawLocation == null) {
            return location;
        }
        location.latitude = rawLocation.getLatitude();
        location.longitude = rawLocation.getLongitude();
        return location;
    }

}
