package com.goodtech.tq.fragement.viewholder;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Daypart;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.WeatherUtils;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class DailyHolder extends RecyclerView.ViewHolder {

    public TextView mDayTv;
    public ImageView mIconImgV;
    public TextView mMaxTempTv;
    public TextView mMinTempTv;

    public DailyHolder(View view) {
        super(view);
        mMaxTempTv = view.findViewById(R.id.tv_max_temp);
        mMinTempTv = view.findViewById(R.id.tv_min_temp);
        mDayTv = view.findViewById(R.id.tv_day_of_week);
        mIconImgV = view.findViewById(R.id.img_icon);
    }

    public static int getResource() {
        return R.layout.weather_item_daily;
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model, Daily daily) {
        if (daily != null) {
            mDayTv.setText(daily.dow);
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

}
