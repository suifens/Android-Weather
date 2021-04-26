package com.goodtech.tq.others.constellation.mode;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * com.goodtech.tq.models
 */
public class ConsYearMode implements Parcelable {


    /**
     * name : 白羊座
     * date : 2016年
     * year : 2016
     * mima : {"info":"变身完成的调整之年","text":["2016年将会是白羊座暂时放缓节奏，开始调整个人生活作息以及细分工作内容的一年。土星 来到射手座对白羊座而言实际属于利好，让你们可以更加客观地看待当下面临的问题，并根据现状调整预 期，为今后相当长一段时间（可能影响未来10年）做好最合适的计划和目..."]}
     * career : ["土星的移位意味着你们的工作重心会有所转移，从前的忙乱筹备已经落实到目标更加明确的层 面。对创业者而言，前景目标相对比较明确，只要按预期计划踏踏实实执行下去就可以。你也可以将更多精 力投入于长远规划以及专业研究、发行出版、异域涉外等方面去，都会获得行业认可的业绩，在相关行业崭 露头角，奠定行业地位。自由职业者则有机会产出一些惊为天人的作品，叫....."]
     * love : ["上半年，木星仍然停留在白羊座的恋爱宫，感情将继续精彩纷呈，尤其容易与旧人擦出火花，展 开异地恋情，同学聚会及老友聚会都是桃花高爆区域，也要小心计划外怀孕。单身人士不乏追求对象，尤其 在3月间可能出现让自己一见钟情的人，但极有可能只是昙花一现的惊心动魄，更像是一场因果牵引的缘分重 聚。4月上旬到中旬则是另一个值得注意的时段，有对象的个人在这两段时间都容...."]
     * finance : ["上半年木星落在投资宫，会给你们带来很好的偏财运。但年后开始的一个月，......."]
     */

    public String name;
    public String date;
    public int year;
    public String[] career;
    public String[] love;
    public String[] finance;

    public ConsYearMode() {}

    protected ConsYearMode(Parcel in) {
        name = in.readString();
        date = in.readString();
        year = in.readInt();

        int careerLength = in.readInt() ;
        if(careerLength>0){
            career = new String[careerLength];
            in.readStringArray(career);
        }

        int loveLength = in.readInt() ;
        if(loveLength>0){
            love = new String[loveLength];
            in.readStringArray(love);
        }

        int financeLength = in.readInt() ;
        if(financeLength>0){
            finance = new String[financeLength];
            in.readStringArray(finance);
        }

    }

    public static final Creator<ConsYearMode> CREATOR = new Creator<ConsYearMode>() {
        @Override
        public ConsYearMode createFromParcel(Parcel in) {
            return new ConsYearMode(in);
        }

        @Override
        public ConsYearMode[] newArray(int size) {
            return new ConsYearMode[size];
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
        dest.writeInt(year);

        if(career==null){
            dest.writeInt(0);
        }else{
            dest.writeInt(career.length);
            dest.writeStringArray(career);
        }

        if(love==null){
            dest.writeInt(0);
        }else{
            dest.writeInt(love.length);
            dest.writeStringArray(love);
        }

        if(finance==null){
            dest.writeInt(0);
        }else{
            dest.writeInt(finance.length);
            dest.writeStringArray(finance);
        }
    }
}
