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
     * "province": "安徽省",
     * "city": "合肥",
     * "lat": "31.52",
     * "lon": "117.17",
     * "pinyin": "he fei"
     */
    public int cid = 0;

    public String province;

    public String city;

    public String lat;

    public String lon;

    public String pinyin;

    public int listNum;

    public boolean location = false;

    public CityMode() {}

    protected CityMode(Parcel in) {
        cid = in.readInt();
        province = in.readString();
        city = in.readString();
        lat = in.readString();
        lon = in.readString();
        pinyin = in.readString();
        listNum = in.readInt();
        location = in.readInt() == 0;
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
        this.cid = cursor.getInt(cursor.getColumnIndex("cid"));
        this.province = cursor.getString(cursor.getColumnIndex("province"));
        this.city = cursor.getString(cursor.getColumnIndex("city"));
        this.lat = cursor.getString(cursor.getColumnIndex("lat"));
        this.lon = cursor.getString(cursor.getColumnIndex("lon"));
        this.pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(cid);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(lat);
        dest.writeString(lon);
        dest.writeString(pinyin);
        dest.writeInt(listNum);
        dest.writeInt(location ? 1 : 0);
    }
}
