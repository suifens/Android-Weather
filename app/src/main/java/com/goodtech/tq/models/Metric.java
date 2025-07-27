package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 气象要素数据类
 * 包含温度、湿度、气压、风速等具体的数值数据
 * 实现Parcelable接口，支持数据序列化传输
 */
public class Metric implements Parcelable {

    /** 阵风风速（公里/小时） */
    public int gust;
    
    /** 露点温度（摄氏度），空气中水汽达到饱和时的温度 */
    @SerializedName("dewpt")
    public int dewpt;
    
    /** 体感温度（摄氏度），考虑风速和湿度影响后的实际感受温度 */
    @SerializedName("feels_like")
    public int feelsLike;
    
    /** 最高温度（摄氏度） */
    @SerializedName("max_temp")
    public int maxTemp;
    
    /** 最低温度（摄氏度） */
    @SerializedName("min_temp")
    public int minTemp;
    
    /** 累计降水量（毫米） */
    @SerializedName("precip_total")
    public float precipTotal;
    
    /** 气压值（毫巴） */
    @SerializedName("pressure")
    public float pressure;
    
    /** 当前温度（摄氏度） */
    @SerializedName("temp")
    public int temp;
    
    /** 能见度（公里） */
    @SerializedName("vis")
    public float vis;
    
    /** 风速（公里/小时） */
    @SerializedName("wspd")
    public int wspd;

    /**
     * 重写toString方法，用于调试输出
     * @return 格式化的字符串表示
     */
    @Override
    public String toString() {
        return "Metric {" + "\n" +
                "gust = " + gust + "\n" +
                "dewpt = " + dewpt + "\n" +
                "feelsLike = " + feelsLike + "\n" +
                "maxTemp = " + maxTemp + "\n" +
                "minTemp = " + minTemp + "\n" +
                "precipTotal = " + precipTotal + "\n" +
                "pressure = " + pressure + "\n" +
                "temp = " + temp + "\n" +
                "vis = " + vis + "\n" +
                "wspd = " + wspd + "\n" +
                '}';
    }

    /**
     * Parcelable构造函数
     * @param in Parcel对象
     */
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

    /** 默认构造函数 */
    Metric() {

    }

    /** Parcelable创建器 */
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
