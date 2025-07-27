package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.goodtech.tq.utils.TimeUtils;
import com.google.gson.annotations.SerializedName;

/**
 * 日天气预报数据类
 * 包含未来10天内每天的详细天气信息，包括白天和夜间天气
 * 实现Parcelable接口，支持数据序列化传输
 */
public class Daily implements Parcelable {
    /** 预报序号 */
    @SerializedName("num")
    public int num;

    /** 星期几，如"Monday"、"Tuesday"等 */
    @SerializedName("dow")
    public String dow;

    /** 预报有效时间戳（秒） */
    @SerializedName("fcst_valid")
    public long fcst_valid;

    /** 预报有效时间（本地时间字符串） */
    @SerializedName("fcst_valid_local")
    public String fcst_valid_local;

    /** 气象要素数据，包含温度、湿度、气压等 */
    @SerializedName("metric")
    public Metric metric = new Metric();

    /** 月出时间 */
    @SerializedName("moonrise")
    public String moonRise;
    
    /** 月落时间 */
    @SerializedName("moonset")
    public String moonSet;
    
    /** 月相描述，如"满月"、"新月"等 */
    @SerializedName("moon_phase")
    public String moon_phase;
    
    /** 月相代码 */
    @SerializedName("moon_phase_code")
    public String moon_phase_code;

    /** 日出时间 */
    @SerializedName("sunrise")
    public String sunRise;
    
    /** 日落时间 */
    @SerializedName("sunset")
    public String sunSet;

    /** 白天天气数据 */
    @SerializedName("day")
    public Daypart dayPart = new Daypart();
    
    /** 夜间天气数据 */
    @SerializedName("night")
    public Daypart nightPart;

    /**
     * 重写toString方法，用于调试输出
     * @return 格式化的字符串表示
     */
    @Override
    public String toString() {
        return "Daily {" + "\n" +
                "metric = " + metric.toString() + "\n" +
                "num = " + num + "\n" +
                "dow = " + dow + "\n" +
                "fcst_valid = " + fcst_valid + "\n" +
                "fcst_valid_local = " + fcst_valid_local + "\n" +
                "moonRise = " + moonRise + "\n" +
                "moonSet = " + moonSet + "\n" +
                "moon_phase = " + moon_phase + "\n" +
                "moon_phase_code = " + moon_phase_code + "\n" +
                "sunRise = " + sunRise + "\n" +
                "sunSet = " + sunSet + "\n" +
                '}';
    }

    /**
     * 获取格式化的日期时间字符串
     * @return 格式为"MM月dd日"的日期字符串
     */
    public String getTime() {
        return TimeUtils.longToString(fcst_valid * 1000, "MM月dd日");
    }

    /**
     * Parcelable构造函数
     * @param in Parcel对象
     */
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

    /** Parcelable创建器 */
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
