package com.goodtech.tq.others.constellation.mode;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * com.goodtech.tq.models
 */
public class ConsMonthMode implements Parcelable {


    /**
     * date : 2016年12月
     * name : 白羊座
     * all : 本月运势有两个重要的节点，一个是在上旬，水星进入事业宫，更加关注事业发展，目标性加 强；而金星随之离开事业宫，原先的经验不能再为你赢得加分，反而是人脉上。。。
     * happyMagic : 
     * health : 上旬和中旬，运动能量高，适合开展锻炼计划，尤其是练习耐力的运动。下旬，水逆开启，出行要小心意外了。
     * love : 现实的比较太累，你更喜欢朋友式的轻松相处，如果和爱人之间做不到，你会更眷恋友人的陪 伴。因而本月“友情已达，恋人未满”的状况，会有更大的发生几率。
     * money : 人际生财，多往人气旺的地方是有利打听到财富资讯，广开财路的。虽然人际开销也会增多 ，但可以当做是投资。
     * month : 12
     * work : 本月的目标性和计划性都很强，两个阶段的区别在于行动力。上旬和中旬，行动力分散，下 旬，行动力足够，但受水逆影响，意外多。
     */

    public String date;
    public String name;
    public int month;
    public String all;
    public String happyMagic;
    public String health;
    public String love;
    public String money;
    public String work;

    public ConsMonthMode() {}

    protected ConsMonthMode(Parcel in) {
        name = in.readString();
        date = in.readString();
        all = in.readString();
        health = in.readString();
        happyMagic = in.readString();
        love = in.readString();
        money = in.readString();
        work = in.readString();
        month = in.readInt();
    }

    public static final Creator<ConsMonthMode> CREATOR = new Creator<ConsMonthMode>() {
        @Override
        public ConsMonthMode createFromParcel(Parcel in) {
            return new ConsMonthMode(in);
        }

        @Override
        public ConsMonthMode[] newArray(int size) {
            return new ConsMonthMode[size];
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
        dest.writeString(all);
        dest.writeString(health);
        dest.writeString(happyMagic);
        dest.writeString(love);
        dest.writeString(money);
        dest.writeString(work);
        dest.writeInt(month);
    }
}
