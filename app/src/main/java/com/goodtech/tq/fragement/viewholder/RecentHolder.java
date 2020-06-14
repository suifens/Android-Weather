package com.goodtech.tq.fragement.viewholder;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Daypart;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.TimeUtils;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class RecentHolder extends RecyclerView.ViewHolder {

    public TextView mTTempTv;
    public TextView mTPhraseTv;
    public TextView mTQualityTv;
    //  明天
    public TextView mMTempTv;
    public TextView mMPhraseTv;
    public TextView mMQualityTv;

    public RecentHolder(View view) {
        super(view);
        mTTempTv = view.findViewById(R.id.tv_temperature_today);
        mTPhraseTv = view.findViewById(R.id.tv_weather_today);
        mTQualityTv = view.findViewById(R.id.tv_quality_today);

        mMTempTv = view.findViewById(R.id.tv_temperature_morn);
        mMPhraseTv = view.findViewById(R.id.tv_weather_morn);
        mMQualityTv = view.findViewById(R.id.tv_quality_morn);
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel weatherModel) {
        if (weatherModel != null
                && weatherModel.dailies != null
                && weatherModel.dailies.size() > 2) {

            Daily today = weatherModel.dailies.get(0);
            Daily tomorrow = weatherModel.dailies.get(1);

            long currentTime = System.currentTimeMillis();
            long sunSetTime = TimeUtils.switchTime(today.sunSet);
            boolean day = currentTime < sunSetTime;

            Daypart todayPart = day ? today.dayPart : today.nightPart;
            Daypart tomorrowPart = day ? tomorrow.dayPart : tomorrow.nightPart;

            mTTempTv.setText(String.format("%d/%d℃", today.metric.maxTemp, today.metric.minTemp));
            mTPhraseTv.setText(todayPart.wdirCardinal);

            mMTempTv.setText(String.format("%d/%d℃", tomorrow.metric.maxTemp, tomorrow.metric.minTemp));
            mMPhraseTv.setText(tomorrowPart.wdirCardinal);
        }
    }

}
