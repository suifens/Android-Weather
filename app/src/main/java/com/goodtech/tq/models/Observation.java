package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class Observation implements Parcelable {
    @SerializedName("valid_time_gmt")
    public long validTime;

    @SerializedName("metric.dewpt")
    public int dewpt;
    @SerializedName("metric.feels_like")
    public int feelsLike;
    @SerializedName("metric.max_temp")
    public int maxTemp;
    @SerializedName("metric.min_temp")
    public int minTemp;
    @SerializedName("metric.precip_total")
    public float precipTotal;
    @SerializedName("metric.pressure")
    public float pressure;
    @SerializedName("metric.temp")
    public int temp;
    @SerializedName("metric.vis")
    public float vis;
    @SerializedName("metric.wspd")
    public int wspd;

    @SerializedName("obs_id")
    public String obsId;
    @SerializedName("obs_name")
    public String obsName;
    @SerializedName("pressure_desc")
    public String pressureDesc;
    @SerializedName("pressure_tend")
    public int pressureTend;
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
    @SerializedName("wx_icon")
    public int wxIcon;
    @SerializedName("wx_phrase")
    public String wxPhrase;

    protected Observation(Parcel in) {
        validTime = in.readLong();
        dewpt = in.readInt();
        feelsLike = in.readInt();
        maxTemp = in.readInt();
        minTemp = in.readInt();
        precipTotal = in.readFloat();
        pressure = in.readFloat();
        temp = in.readInt();
        vis = in.readFloat();
        wspd = in.readInt();
        obsId = in.readString();
        obsName = in.readString();
        pressureDesc = in.readString();
        pressureTend = in.readInt();
        rh = in.readInt();
        uvDesc = in.readString();
        uvInde = in.readInt();
        wdir = in.readInt();
        wdirCardinal = in.readString();
        wxIcon = in.readInt();
        wxPhrase = in.readString();
    }

    public static final Creator<Observation> CREATOR = new Creator<Observation>() {
        @Override
        public Observation createFromParcel(Parcel in) {
            return new Observation(in);
        }

        @Override
        public Observation[] newArray(int size) {
            return new Observation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(validTime);
        dest.writeInt(dewpt);
        dest.writeInt(feelsLike);
        dest.writeInt(maxTemp);
        dest.writeInt(minTemp);
        dest.writeFloat(precipTotal);
        dest.writeFloat(pressure);
        dest.writeInt(temp);
        dest.writeFloat(vis);
        dest.writeInt(wspd);
        dest.writeString(obsId);
        dest.writeString(obsName);
        dest.writeString(pressureDesc);
        dest.writeInt(pressureTend);
        dest.writeInt(rh);
        dest.writeString(uvDesc);
        dest.writeInt(uvInde);
        dest.writeInt(wdir);
        dest.writeString(wdirCardinal);
        dest.writeInt(wxIcon);
        dest.writeString(wxPhrase);
    }
}
