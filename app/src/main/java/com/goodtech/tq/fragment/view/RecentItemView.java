package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.goodtech.tq.R;
import com.goodtech.tq.helpers.AqiHelper;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Daypart;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.TimeUtils;


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class RecentItemView extends LinearLayout {

    public RecentItemView(Context context) {
        this(context, null);
    }

    public RecentItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecentItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public TextView mNoticeTv;

    public TextView mTTempTv;
    public TextView mTPhraseTv;
    public TextView mTQualityTv;
    //  明天
    public TextView mMTempTv;
    public TextView mMPhraseTv;
    public TextView mMQualityTv;
    
    @SuppressLint("DefaultLocale")
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_recent, this, true);

        mNoticeTv = view.findViewById(R.id.tv_notice);

        mTTempTv = view.findViewById(R.id.tv_temperature_today);
        mTPhraseTv = view.findViewById(R.id.tv_weather_today);
        mTQualityTv = view.findViewById(R.id.tv_quality_today);

        mMTempTv = view.findViewById(R.id.tv_temperature_morn);
        mMPhraseTv = view.findViewById(R.id.tv_weather_morn);
        mMQualityTv = view.findViewById(R.id.tv_quality_morn);
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel weatherModel) {
        if (weatherModel != null) {

            Observation observation = weatherModel.observation;
            Metric metric = observation.metric;

            Daily today = weatherModel.today();
            if (today != null) {
                mNoticeTv.setText(String.format("今天：当前%s，最高气温%d°，最低气温%d°", observation.getWxPhrase(),
                        today.metric.maxTemp, today.metric.minTemp));
            } else {
                mNoticeTv.setText(String.format("今天：当前%s，最高气温%d°，最低气温%d°", observation.getWxPhrase(),
                        metric.maxTemp, metric.minTemp));
            }

            if (today != null) {
                long currentTime = System.currentTimeMillis();
                long sunSetTime = TimeUtils.switchTime(today.sunSet);
                boolean day = currentTime < sunSetTime;

                Daypart todayPart = day ? today.dayPart : today.nightPart;
                mTTempTv.setText(String.format("%d°/%d°", today.metric.maxTemp, today.metric.minTemp));
                if (todayPart != null) {
                    if (!todayPart.getPhraseChar().isEmpty()) {
                        mTPhraseTv.setText(todayPart.getPhraseChar());
                    } else {
                        mTPhraseTv.setText(observation.getWxPhrase());
                    }
                }

                if (weatherModel.aqi > 0) {
                    mTQualityTv.setVisibility(VISIBLE);
                    mTQualityTv.setText(AqiHelper.getQuality(weatherModel.aqi));
                    mTQualityTv.setBackgroundResource(AqiHelper.getBgColorResId(weatherModel.aqi));
                } else {
                    mTQualityTv.setVisibility(GONE);
                }

                Daily tomorrow = weatherModel.tomorrow();
                if (tomorrow != null) {
                    Daypart tomorrowPart = day ? tomorrow.dayPart : tomorrow.nightPart;
                    mMTempTv.setText(String.format("%d°/%d°", tomorrow.metric.maxTemp, tomorrow.metric.minTemp));
                    if (tomorrowPart != null) mMPhraseTv.setText(tomorrowPart.getPhraseChar());
                }
            }
        }
    }

}
