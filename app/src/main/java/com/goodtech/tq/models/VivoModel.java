package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * com.goodtech.tq.models
 */
public class VivoModel implements Serializable, Parcelable {
    public String access_token;
    public String refresh_token;
    public Long token_date;
    public Long refresh_token_date;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.access_token);
        dest.writeString(this.refresh_token);
        dest.writeValue(this.token_date);
        dest.writeValue(this.refresh_token_date);
    }

    public void readFromParcel(Parcel source) {
        this.access_token = source.readString();
        this.refresh_token = source.readString();
        this.token_date = (Long) source.readValue(Long.class.getClassLoader());
        this.refresh_token_date = (Long) source.readValue(Long.class.getClassLoader());
    }

    public VivoModel() {
    }

    protected VivoModel(Parcel in) {
        this.access_token = in.readString();
        this.refresh_token = in.readString();
        this.token_date = (Long) in.readValue(Long.class.getClassLoader());
        this.refresh_token_date = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Parcelable.Creator<VivoModel> CREATOR = new Parcelable.Creator<VivoModel>() {
        @Override
        public VivoModel createFromParcel(Parcel source) {
            return new VivoModel(source);
        }

        @Override
        public VivoModel[] newArray(int size) {
            return new VivoModel[size];
        }
    };
}
