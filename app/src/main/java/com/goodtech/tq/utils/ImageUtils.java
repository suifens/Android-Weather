package com.goodtech.tq.utils;

import com.goodtech.tq.R;

/**
 * com.goodtech.tq.utils
 */
public class ImageUtils {

    public static int weatherImageRes(int icon_cd) {
        switch (icon_cd) {
            case 0:
                return R.drawable.icon0;
            case 1:
                return R.drawable.icon1;
            case 2:
                return R.drawable.icon2;
            case 3:
                return R.drawable.icon3;
            case 4:
                return R.drawable.icon4;
            case 5:
                return R.drawable.icon5;
            case 6:
                return R.drawable.icon6;
            case 7:
                return R.drawable.icon7;
            case 8:
                return R.drawable.icon8;
            case 9:
                return R.drawable.icon9;
            case 10:
                return R.drawable.icon10;
            case 11:
                return R.drawable.icon11;
            case 12:
                return R.drawable.icon12;
            case 13:
                return R.drawable.icon13;
            case 14:
                return R.drawable.icon14;
            case 15:
                return R.drawable.icon15;
            case 16:
                return R.drawable.icon16;
            case 17:
                return R.drawable.icon17;
            case 18:
                return R.drawable.icon18;
            case 19:
                return R.drawable.icon19;
            case 20:
                return R.drawable.icon20;
            case 21:
                return R.drawable.icon21;
            case 22:
                return R.drawable.icon22;
            case 23:
                return R.drawable.icon23;
            case 24:
                return R.drawable.icon24;
            case 25:
                return R.drawable.icon25;
            case 26:
                return R.drawable.icon26;
            case 27:
                return R.drawable.icon27;
            case 28:
                return R.drawable.icon28;
            case 29:
                return R.drawable.icon29;
            case 30:
                return R.drawable.icon30;
            case 31:
                return R.drawable.icon31;
            case 32:
                return R.drawable.icon32;
            case 33:
                return R.drawable.icon33;
            case 34:
                return R.drawable.icon34;
            case 35:
                return R.drawable.icon35;
            case 36:
                return R.drawable.icon36;
            case 37:
                return R.drawable.icon37;
            case 38:
                return R.drawable.icon38;
            case 39:
                return R.drawable.icon39;
            case 40:
                return R.drawable.icon40;
            case 41:
                return R.drawable.icon41;
            case 42:
                return R.drawable.icon42;
            case 43:
                return R.drawable.icon43;
            case 44:
                return R.drawable.icon44;
            case 45:
                return R.drawable.icon45;
            case 46:
                return R.drawable.icon46;
            default:
                return R.drawable.icon47;
        }
    }

    public static int bgImageRes(int icon_cd, boolean night) {
        switch (icon_cd) {
            case 0:
            case 19:
            case 20:
            case 21:
            case 22:
                return R.drawable.bg_0;
            case 1:
            case 2:
            case 3:
            case 4:
            case 8:
            case 9:
            case 11:
            case 12:
            case 37:
            case 38:
            case 39:
            case 40:
            case 45:
            case 47:
                return night ? R.drawable.bg_yutian_night : R.drawable.bg_yutian;
            case 5:
            case 6:
            case 7:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 25:
            case 35:
            case 41:
            case 42:
            case 43:
            case 46:
                return night ? R.drawable.bg_xiaxue_night : R.drawable.bg_xiaxue;
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 33:
            case 34:
                return night ? R.drawable.bg_yintian_duoyun_night : R.drawable.bg_duoyun;
            //  晴
            case 23:
            case 24:
            case 31:
            case 32:
            case 36:
                return night ? R.drawable.bg_qin_night : R.drawable.bg_qintian;
            case 44:
                return night ? R.drawable.bg_yintian_duoyun_night : R.drawable.bg_yintian;
            default:
                return R.drawable.bg_normal;
        }
    }

}
