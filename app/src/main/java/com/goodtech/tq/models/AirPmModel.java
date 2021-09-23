package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;

import com.goodtech.tq.R;
import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class AirPmModel implements Parcelable {

    private String city;
    @SerializedName("PM2.5")
    private String pm25;
    @SerializedName("AQI")
    private String aqi;
    private String quality;
    @SerializedName("PM10")
    private String pm10;
    @SerializedName("CO")
    private String co;
    @SerializedName("NO2")
    private String no2;
    @SerializedName("O3")
    private String ozone;
    @SerializedName("SO2")
    private String so2;
    private String time;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
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

    public String getPm10() {
        return pm10;
    }

    public void setPm10(String pm10) {
        this.pm10 = pm10;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getNo2() {
        return no2;
    }

    public void setNo2(String no2) {
        this.no2 = no2;
    }

    public String getOzone() {
        return ozone;
    }

    public void setOzone(String ozone) {
        this.ozone = ozone;
    }

    public String getSo2() {
        return so2;
    }

    public void setSo2(String so2) {
        this.so2 = so2;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    /**
     * 获取提示语
     */
    public String getRemindString() {
        int pm = Integer.parseInt(pm25);
        if (pm > 200) {
            //  重度
            return "健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病";
        } else if (pm > 150) {
            //  中度
            return "易感人群症状进一步加剧，可能对健康人群心脏、呼吸系统有影响";
        } else if (pm > 100) {
            //  轻度
            return "易感人群症状有轻度加剧，健康人群出现刺激症状";
        } else if (pm > 50) {
            //  良
            return "空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响";
        } else {
            return "空气质量令人满意，基本无空气污染，各类人可正常活动";
        }
    }

    /**
     * 获取背景颜色图片
     */
    public int getImgResId() {
        int pm = Integer.parseInt(pm25);
        if (pm > 200) {
            //  重度
            return R.drawable.bg_zl_gd;
        } else if (pm > 150) {
            //  中度
            return R.drawable.bg_zl_zd;
        } else if (pm > 100) {
            //  轻度
            return R.drawable.bg_zl_qd;
        } else if (pm > 50) {
            //  良
            return R.drawable.bg_zl_lian;
        } else {
            return R.drawable.bg_zl_you;
        }
    }

    public int getColorResId() {
        int pm = Integer.parseInt(pm25);
        if (pm > 200) {
            //  重度
            return R.color.color_ce4c72;
        } else if (pm > 150) {
            //  中度
            return R.color.color_ff6B5f;
        } else if (pm > 100) {
            //  轻度
            return R.color.color_ff8550;
        } else if (pm > 50) {
            //  良
            return R.color.color_e7a139;
        } else {
            return R.color.color_5ecd75;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.city);
        dest.writeString(this.pm25);
        dest.writeString(this.aqi);
        dest.writeString(this.quality);
        dest.writeString(this.pm10);
        dest.writeString(this.co);
        dest.writeString(this.no2);
        dest.writeString(this.ozone);
        dest.writeString(this.so2);
        dest.writeString(this.time);
    }

    public void readFromParcel(Parcel source) {
        this.city = source.readString();
        this.pm25 = source.readString();
        this.aqi = source.readString();
        this.quality = source.readString();
        this.pm10 = source.readString();
        this.co = source.readString();
        this.no2 = source.readString();
        this.ozone = source.readString();
        this.so2 = source.readString();
        this.time = source.readString();
    }

    public AirPmModel() {
    }

    protected AirPmModel(Parcel in) {
        this.city = in.readString();
        this.pm25 = in.readString();
        this.aqi = in.readString();
        this.quality = in.readString();
        this.pm10 = in.readString();
        this.co = in.readString();
        this.no2 = in.readString();
        this.ozone = in.readString();
        this.so2 = in.readString();
        this.time = in.readString();
    }

    public static final Parcelable.Creator<AirPmModel> CREATOR = new Parcelable.Creator<AirPmModel>() {
        @Override
        public AirPmModel createFromParcel(Parcel source) {
            return new AirPmModel(source);
        }

        @Override
        public AirPmModel[] newArray(int size) {
            return new AirPmModel[size];
        }
    };
}
