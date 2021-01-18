package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class Metric implements Parcelable {

    public int gust;
    @SerializedName("dewpt")
    public int dewpt;   //  露点
    @SerializedName("feels_like")
    public int feelsLike;
    @SerializedName("max_temp")
    public int maxTemp; //  最高温
    @SerializedName("min_temp")
    public int minTemp; //  最低温
    @SerializedName("precip_total")
    public float precipTotal;
    @SerializedName("pressure")
    public float pressure;  //  气压
    @SerializedName("temp")
    public int temp;    //  温度
    @SerializedName("vis")
    public float vis;   //  能见度
    @SerializedName("wspd")
    public int wspd;    //  风速 km/h

    protected Metric(Parcel in) {
        gust = in.readInt();
        dewpt = in.readInt();
        feelsLike = in.readInt();
        maxTemp = in.readInt();
        minTemp = in.readInt();
        precipTotal = in.readFloat();
        pressure = in.readFloat();
        temp = in.readInt();
        vis = in.readFloat();
        wspd = in.readInt();
    }

    Metric() {

    }

    public static final Creator<Metric> CREATOR = new Creator<Metric>() {
        @Override
        public Metric createFromParcel(Parcel in) {
            return new Metric(in);
        }

        @Override
        public Metric[] newArray(int size) {
            return new Metric[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(gust);
        dest.writeInt(dewpt);
        dest.writeInt(feelsLike);
        dest.writeInt(maxTemp);
        dest.writeInt(minTemp);
        dest.writeFloat(precipTotal);
        dest.writeFloat(pressure);
        dest.writeInt(temp);
        dest.writeFloat(vis);
        dest.writeInt(wspd);
    }
}
