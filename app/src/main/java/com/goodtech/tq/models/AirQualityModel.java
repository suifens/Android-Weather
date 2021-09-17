package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class AirQualityModel implements Parcelable {

    @SerializedName("citynow")
    private CityQuality cityNow;

    public CityQuality getCityNow() {
        return cityNow;
    }

    public void setCityNow(CityQuality cityNow) {
        this.cityNow = cityNow;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.cityNow, flags);
    }

    public void readFromParcel(Parcel source) {
        this.cityNow = source.readParcelable(CityQuality.class.getClassLoader());
    }

    public AirQualityModel() {
    }

    protected AirQualityModel(Parcel in) {
        this.cityNow = in.readParcelable(CityQuality.class.getClassLoader());
    }

    public static final Creator<AirQualityModel> CREATOR = new Creator<AirQualityModel>() {
        @Override
        public AirQualityModel createFromParcel(Parcel source) {
            return new AirQualityModel(source);
        }

        @Override
        public AirQualityModel[] newArray(int size) {
            return new AirQualityModel[size];
        }
    };


    public static class CityQuality implements Parcelable {

        private String city;
        @SerializedName("AQI")
        private String aqi;
        private String quality;
        private String date;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAqi() {
            return aqi;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        public String getQuality() {
            return quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.city);
            dest.writeString(this.aqi);
            dest.writeString(this.quality);
            dest.writeString(this.date);
        }

        public void readFromParcel(Parcel source) {
            this.city = source.readString();
            this.aqi = source.readString();
            this.quality = source.readString();
            this.date = source.readString();
        }

        public CityQuality() {
        }

        protected CityQuality(Parcel in) {
            this.city = in.readString();
            this.aqi = in.readString();
            this.quality = in.readString();
            this.date = in.readString();
        }

        public static final Creator<CityQuality> CREATOR = new Creator<CityQuality>() {
            @Override
            public CityQuality createFromParcel(Parcel source) {
                return new CityQuality(source);
            }

            @Override
            public CityQuality[] newArray(int size) {
                return new CityQuality[size];
            }
        };
    }
}
