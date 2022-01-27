package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * com.goodtech.tq.models
 */
public class CityCodeMode implements Parcelable {

    private String province_code;
    private String city_name;
    private String city_code;

    public String getProvince_code() {
        return province_code;
    }

    public void setProvince_code(String province_code) {
        this.province_code = province_code;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getCity_code() {
        return city_code;
    }

    public void setCity_code(String city_code) {
        this.city_code = city_code;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.province_code);
        dest.writeString(this.city_name);
        dest.writeString(this.city_code);
    }

    public void readFromParcel(Parcel source) {
        this.province_code = source.readString();
        this.city_name = source.readString();
        this.city_code = source.readString();
    }

    public CityCodeMode() {
    }

    protected CityCodeMode(Parcel in) {
        this.province_code = in.readString();
        this.city_name = in.readString();
        this.city_code = in.readString();
    }

    public static final Creator<CityCodeMode> CREATOR = new Creator<CityCodeMode>() {
        @Override
        public CityCodeMode createFromParcel(Parcel source) {
            return new CityCodeMode(source);
        }

        @Override
        public CityCodeMode[] newArray(int size) {
            return new CityCodeMode[size];
        }
    };
}
