package com.goodtech.tq.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * com.goodtech.tq.models
 */
public class CityMode implements Parcelable {
    /**
     * "id": 10000,
     * "mergerName": "安徽省",
     * "city": "合肥",
     * "lat": "31.52",
     * "lon": "117.17",
     * "pinyin": "he fei"
     */
    private int cid = 0;

    private String mergerName;

    private String city;

    private String lat;

    private String lon;

    private String pinyin;

    private int listNum;

    private boolean location = false;

    private int viewType = 0;

    public boolean getLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getMergerName() {
        return mergerName;
    }

    public void setMergerName(String mergerName) {
        this.mergerName = mergerName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public int getListNum() {
        return listNum;
    }

    public void setListNum(int listNum) {
        this.listNum = listNum;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public CityMode() {}

    protected CityMode(Parcel in) {
        this.cid = in.readInt();
        this.mergerName = in.readString();
        this.city = in.readString();
        this.lat = in.readString();
        this.lon = in.readString();
        this.pinyin = in.readString();
        this.listNum = in.readInt();
        this.location = in.readInt() == 0;
    }

    public static final Creator<CityMode> CREATOR = new Creator<CityMode>() {
        @Override
        public CityMode createFromParcel(Parcel in) {
            return new CityMode(in);
        }

        @Override
        public CityMode[] newArray(int size) {
            return new CityMode[size];
        }
    };

    public void resolveCour(Cursor cursor){
        this.cid = cursor.getInt(cursor.getColumnIndex("id"));
        this.mergerName = cursor.getString(cursor.getColumnIndex("mergerName"));
        this.city = cursor.getString(cursor.getColumnIndex("cityName"));
        this.lat = cursor.getString(cursor.getColumnIndex("latitude"));
        this.lon = cursor.getString(cursor.getColumnIndex("longitude"));
        this.pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.cid);
        dest.writeString(this.mergerName);
        dest.writeString(this.city);
        dest.writeString(this.lat);
        dest.writeString(this.lon);
        dest.writeString(this.pinyin);
        dest.writeInt(this.listNum);
        dest.writeInt(this.location ? 1 : 0);
    }
}
