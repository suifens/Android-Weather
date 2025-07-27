package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 小时天气预报数据类
 * 包含未来24小时内每个小时的详细天气信息
 * 实现Parcelable接口，支持数据序列化传输
 */
public class Hourly implements Parcelable {

    /** 预报序号 */
    @SerializedName("num")
    public int num;
    
    /** 日期标识，区分白天和夜间 */
    @SerializedName("day_ind")
    public String dayInd;
    
    /** 星期几，如"Monday"、"Tuesday"等 */
    @SerializedName("dow")
    public String dow;
    
    /** 气象要素数据，包含温度、湿度、气压等 */
    @SerializedName("metric")
    public Metric metric;
    
    /** 预报有效时间戳（秒） */
    @SerializedName("fcst_valid")
    public long fcst_valid;
    
    /** 预报有效时间（本地时间字符串） */
    @SerializedName("fcst_valid_local")
    public String fcst_valid_local;
    
    /** 天气图标代码 */
    @SerializedName("icon_cd")
    public int icon_cd;
    
    /** 扩展天气图标代码 */
    @SerializedName("icon_extd")
    public int icon_extd;
    
    /** 天气现象描述，32字符限制 */
    @SerializedName("phrase_32char")
    public String phraseChar;
    
    /** 降水概率（百分比） */
    @SerializedName("pop")
    public int pop;
    
    /** 降水类型，如"rain"、"snow"等 */
    @SerializedName("precip_type")
    public String precip_type;
    
    /** 相对湿度（百分比） */
    @SerializedName("rh")
    public int rh;
    
    /** 紫外线强度描述，如"低"、"中"、"高" */
    @SerializedName("uv_desc")
    public String uv_desc;
    
    /** 紫外线指数，数值越大表示紫外线越强 */
    @SerializedName("uv_index")
    public int uv_index;
    
    /** 风向角度（度） */
    @SerializedName("wdir")
    public int wdir;
    
    /** 风向方位描述，如"东北偏东"、"西南风"等 */
    @SerializedName("wdir_cardinal")
    public String wdir_cardinal;

    /** 是否为日出时间 */
    public boolean sunrise;
    
    /** 是否为日落时间 */
    public boolean sunset;

    /**
     * 获取天气现象描述
     * 特殊处理阴天天气，统一返回"阴天"
     * @return 天气现象描述字符串
     */
    public String getPhraseChar() {
        if (icon_cd == 26) return "阴天";
        return phraseChar;
    }

    /**
     * 重写toString方法，用于调试输出
     * @return 格式化的字符串表示
     */
    @Override
    public String toString() {
        return "Hourly {" + "\n" +
                "num = " + num + "\n" +
                "dayInd = " + dayInd + "\n" +
                "dow = " + dow + "\n" +
                "metric = " + metric.toString() + "\n" +
                "fcst_valid = " + fcst_valid + "\n" +
                "fcst_valid_local = " + fcst_valid_local + "\n" +
                "icon_cd = " + icon_cd + "\n" +
                "icon_extd = " + icon_extd + "\n" +
                "wdir = " + wdir + "\n" +
                "phraseChar = " + phraseChar + "\n" +
                "pop = " + pop + "\n" +
                "precip_type = " + precip_type + "\n" +
                "rh = " + rh + "\n" +
                "uv_desc = " + uv_desc + "\n" +
                "uv_index = " + uv_index + "\n" +
                "wdir_cardinal = " + wdir_cardinal + "\n" +
                '}';
    }

    /** 默认构造函数 */
    public Hourly() {

    }

    /**
     * Parcelable构造函数
     * @param in Parcel对象
     */
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

    /** Parcelable创建器 */
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
