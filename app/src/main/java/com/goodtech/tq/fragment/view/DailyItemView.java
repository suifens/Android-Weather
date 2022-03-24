package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Daypart;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.WeatherUtils;


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class DailyItemView extends ConstraintLayout {

    public DailyItemView(Context context) {
        this(context, null);
    }

    public DailyItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DailyItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public TextView mDayTv;
    public ImageView mIconImgV;
    public TextView mMaxTempTv;
    public TextView mMinTempTv;
    
    @SuppressLint("DefaultLocale")
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_daily, this, true);
        mMaxTempTv = view.findViewById(R.id.tv_max_temp);
        mMinTempTv = view.findViewById(R.id.tv_min_temp);
        mDayTv = view.findViewById(R.id.tv_day_of_week);
        mIconImgV = view.findViewById(R.id.img_icon);
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model, Daily daily) {
        if (daily != null) {
            setVisibility(VISIBLE);
            mDayTv.setText(dayString(daily.fcst_valid * 1000, daily.dow));
            mMaxTempTv.setText(String.format("%d℃", daily.metric.maxTemp));
            mMinTempTv.setText(String.format("%d℃", daily.metric.minTemp));

            long currentTime = System.currentTimeMillis();
            long sunSetTime = TimeUtils.switchTime(daily.sunSet);
            boolean day = currentTime < sunSetTime;

            Daypart dayPart = day ? daily.dayPart : daily.nightPart;
            if (dayPart == null) {
                Observation observation = model.observation;
                if (observation != null) {
                    mIconImgV.setImageResource(ImageUtils.weatherImageRes(observation.wxIcon));
                }
            } else {
                mIconImgV.setImageResource(ImageUtils.weatherImageRes(dayPart.iconCd));
            }
        }
    }

    private String dayString(long time, String dow) {
        int timeDay = Integer.parseInt(TimeUtils.timeToDay(time));
        int today = Integer.parseInt(TimeUtils.timeToDay(System.currentTimeMillis()));
        String timeString = TimeUtils.longToString(time, "MM月dd日");
        switch (timeDay - today) {
            case -1:
                return String.format("昨  天 (%s)", timeString);
            case 0:
                return String.format("今  天 (%s)", timeString);
            case 1:
                return String.format("明  天 (%s)", timeString);
            default:
                return String.format("%s (%s)", dow, timeString);
        }
    }

}
