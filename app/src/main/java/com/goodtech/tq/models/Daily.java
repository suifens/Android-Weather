package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class Daily implements Parcelable {
    @SerializedName("num")
    public int num;

    @SerializedName("dow")
    public String dow;

    @SerializedName("fcst_valid")
    public long fcst_valid;

    @SerializedName("fcst_valid_local")
    public String fcst_valid_local;

    @SerializedName("metric")
    public Metric metric;

    @SerializedName("moonrise")
    public String moonRise;
    @SerializedName("moonset")
    public String moonSet;
    @SerializedName("moon_phase")
    public String moon_phase;
    @SerializedName("moon_phase_code")
    public String moon_phase_code;

    @SerializedName("sunrise")
    public String sunRise;
    @SerializedName("sunset")
    public String sunSet;

    @SerializedName("day")
    public Daypart dayPart;
    @SerializedName("night")
    public Daypart nightPart;

    protected Daily(Parcel in) {
        num = in.readInt();
        dow = in.readString();
        fcst_valid = in.readLong();
        fcst_valid_local = in.readString();
        metric = in.readParcelable(Metric.class.getClassLoader());
        moonRise = in.readString();
        moonSet = in.readString();
        moon_phase = in.readString();
        moon_phase_code = in.readString();
        sunRise = in.readString();
        sunSet = in.readString();
        dayPart = in.readParcelable(Daypart.class.getClassLoader());
        nightPart = in.readParcelable(Daypart.class.getClassLoader());
    }

    public static final Creator<Daily> CREATOR = new Creator<Daily>() {
        @Override
        public Daily createFromParcel(Parcel in) {
            return new Daily(in);
        }

        @Override
        public Daily[] newArray(int size) {
            return new Daily[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(num);
        dest.writeString(dow);
        dest.writeLong(fcst_valid);
        dest.writeString(fcst_valid_local);
        dest.writeParcelable(metric, flags);
        dest.writeString(moonRise);
        dest.writeString(moonSet);
        dest.writeString(moon_phase);
        dest.writeString(moon_phase_code);
        dest.writeString(sunRise);
        dest.writeString(sunSet);
        dest.writeParcelable(dayPart, flags);
        dest.writeParcelable(nightPart, flags);
    }
}
