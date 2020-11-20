package com.goodtech.tq.fragment.viewholder;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
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
        mTTempTv = view.findViewById(R.id.tv_temperature_today);
        mTPhraseTv = view.findViewById(R.id.tv_weather_today);
        mTQualityTv = view.findViewById(R.id.tv_quality_today);

        mMTempTv = view.findViewById(R.id.tv_temperature_morn);
        mMPhraseTv = view.findViewById(R.id.tv_weather_morn);
        mMQualityTv = view.findViewById(R.id.tv_quality_morn);
    }

    public static int gerResource() {
        return R.layout.weather_item_recent;
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel weatherModel) {
        if (weatherModel != null) {

            Daily today = weatherModel.today();
            if (today != null) {
                long currentTime = System.currentTimeMillis();
                long sunSetTime = TimeUtils.switchTime(today.sunSet);
                boolean day = currentTime < sunSetTime;

                Daypart todayPart = day ? today.dayPart : today.nightPart;
                mTTempTv.setText(String.format("%d/%d℃", today.metric.maxTemp, today.metric.minTemp));
                if (todayPart != null) mTPhraseTv.setText(todayPart.phraseChar);

                Daily tomorrow = weatherModel.tomorrow();
                if (tomorrow != null) {
                    Daypart tomorrowPart = day ? tomorrow.dayPart : tomorrow.nightPart;
                    mMTempTv.setText(String.format("%d/%d℃", tomorrow.metric.maxTemp, tomorrow.metric.minTemp));
                    if (tomorrowPart != null) mMPhraseTv.setText(tomorrowPart.phraseChar);
                }
            }
        }
    }

}
