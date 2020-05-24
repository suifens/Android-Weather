package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class Hourly implements Parcelable {

    @SerializedName("num")
    public int num;
    @SerializedName("day_ind")
    public String dayInd;
    @SerializedName("dow")
    public String dow;
    @SerializedName("metric")
    public Metric metric;
    @SerializedName("fcst_valid")
    public long fcst_valid;
    @SerializedName("fcst_valid_local")
    public String fcst_valid_local;
    @SerializedName("icon_cd")
    public int icon_cd;
    @SerializedName("icon_extd")
    public int icon_extd;
    @SerializedName("phrase_32char")
    public String phraseChar;
    @SerializedName("pop")
    public int pop;
    @SerializedName("precip_type")
    public String precip_type;
    @SerializedName("rh")
    public int rh;
    @SerializedName("uv_desc")
    public String uv_desc;
    @SerializedName("uv_index")
    public int uv_index;
    @SerializedName("wdir")
    public int wdir;
    @SerializedName("wdir_cardinal")
    public String wdir_cardinal;

    protected Hourly(Parcel in) {
        num = in.readInt();
        dayInd = in.readString();
        dow = in.readString();
        metric = in.readParcelable(Metric.class.getClassLoader());
        fcst_valid = in.readLong();
        fcst_valid_local = in.readString();
        icon_cd = in.readInt();
        icon_extd = in.readInt();
        phraseChar = in.readString();
        pop = in.readInt();
        precip_type = in.readString();
        rh = in.readInt();
        uv_desc = in.readString();
        uv_index = in.readInt();
        wdir = in.readInt();
        wdir_cardinal = in.readString();
    }

    public static final Creator<Hourly> CREATOR = new Creator<Hourly>() {
        @Override
        public Hourly createFromParcel(Parcel in) {
            return new Hourly(in);
        }

        @Override
        public Hourly[] newArray(int size) {
            return new Hourly[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(num);
        dest.writeString(dayInd);
        dest.writeString(dow);
        dest.writeParcelable(metric, flags);
        dest.writeLong(fcst_valid);
        dest.writeString(fcst_valid_local);
        dest.writeInt(icon_cd);
        dest.writeInt(icon_extd);
        dest.writeString(phraseChar);
        dest.writeInt(pop);
        dest.writeString(precip_type);
        dest.writeInt(rh);
        dest.writeString(uv_desc);
        dest.writeInt(uv_index);
        dest.writeInt(wdir);
        dest.writeString(wdir_cardinal);
    }
}
