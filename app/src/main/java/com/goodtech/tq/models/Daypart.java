package com.goodtech.tq.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 日间/夜间天气数据类
 * 包含白天或夜间的详细天气信息，如天气现象、降水概率、风向等
 * 实现Parcelable接口，支持数据序列化传输
 */
public class Daypart implements Parcelable {

    /**
     * 时段标识：1表示白天，2表示夜间
     */
    @SerializedName("num")
    public int num;

    /** 时段名称，如"白天"、"夜间" */
    @SerializedName("daypart_name")
    public String name;
    
    /** 完整时段名称，如"周日晚间"、"周日白天" */
    @SerializedName("long_daypart_name")
    public String longName;
    
    /** 预报有效时间戳（秒） */
    @SerializedName("fcst_valid")
    public long fcst_valid;

    /** 天气图标代码 */
    @SerializedName("icon_cd")
    public int iconCd;
    
    /** 扩展天气图标代码 */
    @SerializedName("icon_extd")
    public int iconExtd;
    
    /** 天气现象描述，如"局部多云"、"晴天"等 */
    @SerializedName("phrase_32char")
    public String phraseChar;

    /**
     * 获取天气现象描述
     * 特殊处理阴天天气，统一返回"阴天"
     * @return 天气现象描述字符串
     */
    public String getPhraseChar() {
        if (iconCd == 26) return "阴天";
        return phraseChar;
    }

    /** 降水类型，如"rain"、"snow"等 */
    @SerializedName("precip_type")
    public String precipType;
    
    /** 降水概率（百分比） */
    @SerializedName("pop")
    public int pop;
    
    /** 相对湿度（百分比） */
    @SerializedName("rh")
    public int rh;

    /** 紫外线强度描述，如"低"、"中"、"高" */
    @SerializedName("uv_desc")
    public String uvDesc;
    
    /** 紫外线指数，数值越大表示紫外线越强 */
    @SerializedName("uv_index")
    public int uvInde;

    /** 风向角度（度） */
    @SerializedName("wdir")
    public int wdir;
    
    /** 风向方位描述，如"东北偏东"、"西南风"等 */
    @SerializedName("wdir_cardinal")
    public String wdirCardinal;
    
    /** 风速（公里/小时） */
    @SerializedName("metric.wspd")
    public int wspd;

    /**
     * 重写toString方法，用于调试输出
     * @return 格式化的字符串表示
     */
    @Override
    public String toString() {
        return "Daypart {" + "\n" +
                "num = " + num + "\n" +
                "name = " + name + "\n" +
                "longName = " + longName + "\n" +
                "fcst_valid = " + fcst_valid + "\n" +
                "iconCd = " + iconCd + "\n" +
                "rh = " + rh + "\n" +
                "uvDesc = " + uvDesc + "\n" +
                "iconExtd = " + iconExtd + "\n" +
                "wdir = " + wdir + "\n" +
                "wdirCardinal = " + wdirCardinal + "\n" +
                "phraseChar = " + phraseChar + "\n" +
                "precipType = " + precipType + "\n" +
                "pop = " + pop + "\n" +
                "uvInde = " + uvInde + "\n" +
                "wspd = " + wspd + "\n" +
                '}';
    }

    /**
     * Parcelable构造函数
     * @param in Parcel对象
     */
    protected Daypart(Parcel in) {
        num = in.readInt();
        name = in.readString();
        longName = in.readString();
        fcst_valid = in.readLong();
        iconCd = in.readInt();
        iconExtd = in.readInt();
        phraseChar = in.readString();
        precipType = in.readString();
        pop = in.readInt();
        rh = in.readInt();
        uvDesc = in.readString();
        uvInde = in.readInt();
        wdir = in.readInt();
        wdirCardinal = in.readString();
        wspd = in.readInt();
    }

    /** 默认构造函数 */
    Daypart() {

    }

    /** Parcelable创建器 */
    public static final Creator<Daypart> CREATOR = new Creator<Daypart>() {
        @Override
        public Daypart createFromParcel(Parcel in) {
            return new Daypart(in);
        }

        @Override
        public Daypart[] newArray(int size) {
            return new Daypart[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(num);
        dest.writeString(name);
        dest.writeString(longName);
        dest.writeLong(fcst_valid);
        dest.writeInt(iconCd);
        dest.writeInt(iconExtd);
        dest.writeString(phraseChar);
        dest.writeString(precipType);
        dest.writeInt(pop);
        dest.writeInt(rh);
        dest.writeString(uvDesc);
        dest.writeInt(uvInde);
        dest.writeInt(wdir);
        dest.writeString(wdirCardinal);
        dest.writeInt(wspd);
    }
}
