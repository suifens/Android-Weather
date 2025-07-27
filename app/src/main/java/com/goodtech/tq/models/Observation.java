package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 天气观测数据类
 * 包含当前时刻的实时天气信息，如温度、湿度、风向、气压等
 * 实现Parcelable接口，支持数据序列化传输
 */
public class Observation implements Parcelable {
    /** 观测时间戳（GMT时间） */
    @SerializedName("valid_time_gmt")
    public long validTime;

    /** 气象要素数据，包含温度、湿度、气压等具体数值 */
    @SerializedName("metric")
    public Metric metric;

    /** 观测站ID */
    @SerializedName("obs_id")
    public String obsId;
    
    /** 观测站名称 */
    @SerializedName("obs_name")
    public String obsName;
    
    /** 气压描述信息 */
    @SerializedName("pressure_desc")
    public String pressureDesc;
    
    /** 气压变化趋势 */
    @SerializedName("pressure_tend")
    public int pressureTend;
    
    /** 相对湿度百分比 */
    public int rh;
    
    /** 紫外线强度描述，如"低"、"中"、"高" */
    @SerializedName("uv_desc")
    public String uvDesc;
    
    /** 紫外线指数，数值越大表示紫外线越强 */
    @SerializedName("uv_index")
    public int uvIndex;
    
    /** 风向角度（度） */
    @SerializedName("wdir")
    public int wdir;
    
    /** 风向方位描述，如"东北偏东"、"西南风"等 */
    @SerializedName("wdir_cardinal")
    public String wdirCardinal;
    
    /** 天气图标代码 */
    @SerializedName("wx_icon")
    public int wxIcon;
    
    /** 天气现象描述，如"晴天"、"多云"、"小雨"等 */
    @SerializedName("wx_phrase")
    public String wxPhrase;

    /**
     * 获取天气现象描述
     * 特殊处理阴天天气，统一返回"阴天"
     * @return 天气现象描述字符串
     */
    public String getWxPhrase() {
        if (wxIcon == 26) return "阴天";
        return wxPhrase;
    }

    /**
     * 重写toString方法，用于调试输出
     * @return 格式化的字符串表示
     */
    @Override
    public String toString() {
        return "Observation {" + "\n" +
                "metric = " + metric.toString() + "\n" +
                "obsId = " + obsId + "\n" +
                "obsName = " + obsName + "\n" +
                "pressureDesc = " + pressureDesc + "\n" +
                "pressureTend = " + pressureTend + "\n" +
                "rh = " + rh + "\n" +
                "uvDesc = " + uvDesc + "\n" +
                "uvIndex = " + uvIndex + "\n" +
                "wdir = " + wdir + "\n" +
                "wdirCardinal = " + wdirCardinal + "\n" +
                "wxIcon = " + wxIcon + "\n" +
                "wxPhrase = " + wxPhrase + "\n" +
                '}';
    }

    /**
     * Parcelable构造函数
     * @param in Parcel对象
     */
    protected Observation(Parcel in) {
        validTime = in.readLong();
        metric = in.readParcelable(Metric.class.getClassLoader());
        obsId = in.readString();
        obsName = in.readString();
        pressureDesc = in.readString();
        pressureTend = in.readInt();
        rh = in.readInt();
        uvDesc = in.readString();
        uvIndex = in.readInt();
        wdir = in.readInt();
        wdirCardinal = in.readString();
        wxIcon = in.readInt();
        wxPhrase = in.readString();
    }

    /** Parcelable创建器 */
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
        dest.writeParcelable(metric, flags);
        dest.writeString(obsId);
        dest.writeString(obsName);
        dest.writeString(pressureDesc);
        dest.writeInt(pressureTend);
        dest.writeInt(rh);
        dest.writeString(uvDesc);
        dest.writeInt(uvIndex);
        dest.writeInt(wdir);
        dest.writeString(wdirCardinal);
        dest.writeInt(wxIcon);
        dest.writeString(wxPhrase);
    }
}
