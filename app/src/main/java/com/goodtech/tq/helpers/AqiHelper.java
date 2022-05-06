package com.goodtech.tq.helpers;

import android.graphics.Color;

import com.goodtech.tq.R;

/**
 * com.goodtech.tq.helpers
 */
public class AqiHelper {

    /**
     * 获取提示语
     */
    public static String getRemindString(int aqi) {
        if (aqi > 200) {
            //  重度
            return "健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病";
        } else if (aqi > 150) {
            //  中度
            return "易感人群症状进一步加剧，可能对健康人群心脏、呼吸系统有影响";
        } else if (aqi > 100) {
            //  轻度
            return "易感人群症状有轻度加剧，健康人群出现刺激症状";
        } else if (aqi > 50) {
            //  良
            return "空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响";
        } else {
            return "空气质量令人满意，基本无空气污染，各类人可正常活动";
        }
    }

    /**
     * 获取背景颜色图片
     */
    public static int getImgResId(int aqi) {
        if (aqi > 200) {
            //  重度
            return R.drawable.bg_zl_gd;
        } else if (aqi > 150) {
            //  中度
            return R.drawable.bg_zl_zd;
        } else if (aqi > 100) {
            //  轻度
            return R.drawable.bg_zl_qd;
        } else if (aqi > 50) {
            //  良
            return R.drawable.bg_zl_lian;
        } else {
            return R.drawable.bg_zl_you;
        }
    }

    public static int getBgColorResId(int aqi) {
        if (aqi > 200) {
            //  重度
            return R.drawable.bg_radius10_ce4c72;
        } else if (aqi > 150) {
            //  中度
            return R.drawable.bg_radius10_ff6b5f;
        } else if (aqi > 100) {
            //  轻度
            return R.drawable.bg_radius10_ff8550;
        } else if (aqi > 50) {
            //  良
            return R.drawable.bg_radius10_e7a139;
        } else {
            return R.drawable.bg_radius10_5ecd75;
        }
    }

    public static int getColorResId(int aqi) {
        if (aqi > 200) {
            //  重度
            return R.color.color_ce4c72;
        } else if (aqi > 150) {
            //  中度
            return R.color.color_ff6b5f;
        } else if (aqi > 100) {
            //  轻度
            return R.color.color_ff8550;
        } else if (aqi > 50) {
            //  良
            return Color.parseColor("#F8BE58");
        } else {
            return R.color.color_5ecd75;
        }
    }

    public static String getQuality(int aqi) {
        if (aqi > 200) {
            //  重度
            return "严重";
        } else if (aqi > 150) {
            //  中度
            return "中度";
        } else if (aqi > 100) {
            //  轻度
            return "轻度";
        } else if (aqi > 50) {
            //  良
            return "良";
        } else {
            return "优";
        }
    }

    public static int getQualityRes(int aqi) {
        if (aqi > 200) {
            //  重度
            return R.drawable.ic_quality_1;
        } else if (aqi > 150) {
            //  中度
            return R.drawable.ic_quality_2;
        } else if (aqi > 100) {
            //  轻度
            return R.drawable.ic_quality_3;
        } else if (aqi > 50) {
            //  良
            return R.drawable.ic_quality_4;
        } else {
            return R.drawable.ic_quality_5;
        }
    }
    
}
