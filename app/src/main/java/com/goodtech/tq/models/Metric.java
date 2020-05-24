package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * com.goodtech.tq.models
 */
public class Metric implements Parcelable {

    public int dewpt;
    public int gust;
    public int temp;
    public float vis;
    public int wspd;

    protected Metric(Parcel in) {
        dewpt = in.readInt();
        gust = in.readInt();
        temp = in.readInt();
        vis = in.readFloat();
        wspd = in.readInt();
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
        dest.writeInt(dewpt);
        dest.writeInt(gust);
        dest.writeInt(temp);
        dest.writeFloat(vis);
        dest.writeInt(wspd);
    }
}
