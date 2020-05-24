package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class Daypart implements Parcelable {

    /**
     * day 1 ; night 2
     */
    @SerializedName("num")
    public int num;

    @SerializedName("daypart_name")
    public String name;
    //  周日晚间、周日白天
    @SerializedName("long_daypart_name")
    public String longName;
    //  时间 1590318000
    @SerializedName("fcst_valid")
    public long fcst_valid;

    //  29
    @SerializedName("icon_cd")
    public int iconCd;
    @SerializedName("icon_extd")
    public int iconExtd;
    //  局部多云
    @SerializedName("phrase_32char")
    public String phraseChar;
    //  "rain"
    @SerializedName("precip_type")
    public String precipType;
    //  20
    @SerializedName("pop")
    public int pop;
    //  湿度 87
    @SerializedName("rh")
    public int rh;

    //  紫外线
    //  "低"
    @SerializedName("uv_desc")
    public String uvDesc;
    //  0
    @SerializedName("uv_index")
    public int uvInde;

    //  69
    @SerializedName("wdir")
    public int wdir;
    //  "东北偏东"
    @SerializedName("wdir_cardinal")
    public String wdirCardinal;
    // 5 风速
    @SerializedName("metric.wspd")
    public int wspd;


    protected Daypart(Parcel in) {
        num = in.readInt();
        name = in.readString();
        longName = in.readString();
        fcst_valid = in.readLong();
        iconCd = in.readInt();
        iconExtd = in.readInt();
        phraseChar = in.readString();
        precipType = in.readString();
        pop = in.readInt();
        rh = in.readInt();
        uvDesc = in.readString();
        uvInde = in.readInt();
        wdir = in.readInt();
        wdirCardinal = in.readString();
        wspd = in.readInt();
    }

    public static final Creator<Daypart> CREATOR = new Creator<Daypart>() {
        @Override
        public Daypart createFromParcel(Parcel in) {
            return new Daypart(in);
        }

        @Override
        public Daypart[] newArray(int size) {
            return new Daypart[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(num);
        dest.writeString(name);
        dest.writeString(longName);
        dest.writeLong(fcst_valid);
        dest.writeInt(iconCd);
        dest.writeInt(iconExtd);
        dest.writeString(phraseChar);
        dest.writeString(precipType);
        dest.writeInt(pop);
        dest.writeInt(rh);
        dest.writeString(uvDesc);
        dest.writeInt(uvInde);
        dest.writeInt(wdir);
        dest.writeString(wdirCardinal);
        dest.writeInt(wspd);
    }
}
