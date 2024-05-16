package com.goodtech.tq.modules.others.constellation;

import com.goodtech.tq.R;

/**
 * com.goodtech.tq.others.constellation
 */
public enum ConstellationEnum {

    BAIYAN(R.drawable.ic_xingzuo_aries, "白羊座", "3.21 - 4.19"),
    JINNIIU(R.drawable.ic_xingzuo_taurus, "金牛座", "3.21 - 4.19"),
    SHUANGZI(R.drawable.ic_xingzuo_genimi, "双子座","5.21 - 6.21"),
    JUXIE(R.drawable.ic_xingzuo_cancer,"巨蟹座","6.22 - 7.22"),
    SHIZI(R.drawable.ic_xingzuo_leonis, "狮子座","7.23 - 8.22"),
    CHUNV(R.drawable.ic_xingzuo_virgo,"处女座","8.23 - 9.22"),
    TIANCHENG(R.drawable.ic_xingzuo_libra,"天秤座","9.23 - 10.23"),
    TIANXIE(R.drawable.ic_xingzuo_scorpius,"天蝎座","10.24 - 11.22"),
    SHESHOU(R.drawable.ic_xingzuo_sagittarius,"射手座","11.23 - 12.21"),
    MOJIE(R.drawable.ic_xingzuo_capricornus,"摩羯座","12.22 - 1.19"),
    SHUIPING(R.drawable.ic_xingzuo_aguarius,"水瓶座","1.20 - 2.18"),
    SHUANGYU(R.drawable.ic_xingzuo_pisces,"双鱼座","2.19 - 3.20");

    public int imageRes;
    public String name;
    public String date;

    ConstellationEnum(int imageRes, String name, String date) {
        this.imageRes = imageRes;
        this.name = name;
        this.date = date;
    }

    public static ConstellationEnum getConstellationEnum(String enumString) {
        for (ConstellationEnum temp : ConstellationEnum.values()) {
            if (temp.toString().equals(enumString))
                return temp;
        }
        return BAIYAN;
    }

}
