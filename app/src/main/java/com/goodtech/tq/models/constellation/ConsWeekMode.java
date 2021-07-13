package com.goodtech.tq.models.constellation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * com.goodtech.tq.models
 */
public class ConsWeekMode implements Parcelable {

    /**
     * name : 白羊座
     * date : 2014年06月29日-2014年07月05日
     * weekth : 27
     * health : 一些白羊座会面临偏头痛、头晕的情况，有可能是劳累过度，也有可能是颈椎负担太大，要注意多多休息
     * job : 水逆在本周结束，之前耽误、错过的出现弥补机会。职场进入休整状态，有调部门或岗位 的可能
     * love : 恋情：之前积累的想法和感受，本周选择说出来。沟通机会增多，亦有可能以争吵的方式出现。 单身的，在聚会闲谈中可望获得更多缘分。
     * money : 财运：虽有自己的理财想法，但总体受控于家人或家族的财务计划。受木星支撑，有机会得到 家人的支援。但是土逆仍然显示你有债务加大的风险。置业房产出现时机，较大可能是家人出首期，你来月 供。
     * work : 工作：水逆在本周结束，之前耽误、错过的出现弥补机会。职场进入休整状态，有调部门或岗位 的可能。
     * resultcode : 200
     * error_code : 0
     */

    public String name;
    public String date;
    public int weekth;
    public String health;
    public String job;
    public String love;
    public String money;
    public String work;

    public ConsWeekMode() {}

    protected ConsWeekMode(Parcel in) {
        name = in.readString();
        date = in.readString();
        weekth = in.readInt();
        health = in.readString();
        job = in.readString();
        love = in.readString();
        money = in.readString();
        work = in.readString();
    }

    public static final Creator<ConsWeekMode> CREATOR = new Creator<ConsWeekMode>() {
        @Override
        public ConsWeekMode createFromParcel(Parcel in) {
            return new ConsWeekMode(in);
        }

        @Override
        public ConsWeekMode[] newArray(int size) {
            return new ConsWeekMode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(date);
        dest.writeInt(weekth);
        dest.writeString(health);
        dest.writeString(job);
        dest.writeString(love);
        dest.writeString(money);
        dest.writeString(work);
    }
}
