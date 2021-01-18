package com.goodtech.tq.widget.weatherview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.utils.font.AlternateBoldTextView;


public class WeatherItemView extends LinearLayout {

    private View rootView;
    private TextView tvWeek;
    private TextView tvDate;
    private TextView tvDayWeather;
    private TextView tvNightWeather;
    private AlternateBoldTextView tvDayTemp;
    private AlternateBoldTextView tvNightTemp;
    private TemperatureView ttvTemp;
    private ImageView ivDayWeather;
    private ImageView ivNightWeather;

    public WeatherItemView(Context context) {
        this(context, null);
    }

    public WeatherItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WeatherItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        rootView = LayoutInflater.from(context).inflate(R.layout.weather_item, null);
        tvWeek = (TextView) rootView.findViewById(R.id.tv_week);
        tvDate = (TextView) rootView.findViewById(R.id.tv_date);
        tvDayWeather = (TextView) rootView.findViewById(R.id.tv_day_weather);
        tvNightWeather = (TextView) rootView.findViewById(R.id.tv_night_weather);
        tvDayTemp = rootView.findViewById(R.id.tv_day_temp);
        tvNightTemp = rootView.findViewById(R.id.tv_night_temp);
        ttvTemp = (TemperatureView) rootView.findViewById(R.id.ttv_day);
        ivDayWeather = (ImageView) rootView.findViewById(R.id.iv_day_weather);
        ivNightWeather = (ImageView) rootView.findViewById(R.id.iv_night_weather);
        //tvAirLevel = (TextView) rootView.findViewById(R.id.tv_air_level);
        rootView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(rootView);
    }

    public void setWeek(String week) {
        if (tvWeek != null)
            tvWeek.setText(week);
    }

    public void setDate(String date) {
        if (tvDate != null)
            tvDate.setText(date);
    }

    public int getTempX() {
        if (ttvTemp != null)
            return (int) ttvTemp.getX();
        return 0;
    }

    public int getTempY() {
        if (ttvTemp != null)
            return (int) ttvTemp.getY();
        return 0;
    }

    public void setDayWeather(String dayWeather) {
        if (tvDayWeather != null)
            tvDayWeather.setText(dayWeather);
    }

    public void setNightWeather(String nightWeather) {
        if (tvNightWeather != null)
            tvNightWeather.setText(nightWeather);
    }

    public void setDayTemp(int dayTemp) {
        if (ttvTemp != null) {
            ttvTemp.setTemperatureDay(dayTemp);
            tvDayTemp.setText(String.format("%dºC", dayTemp));
        }
    }

    public void setNightTemp(int nightTemp) {
        if (ttvTemp != null) {
            ttvTemp.setTemperatureNight(nightTemp);
            tvNightTemp.setText(String.format("%dºC", nightTemp));
        }
    }

    public void setDayImg(int resId) {
        if (ivDayWeather != null)
            ivDayWeather.setImageResource(resId);
    }

    public void setNightImg(int resId) {
        if (ivNightWeather != null)
            ivNightWeather.setImageResource(resId);
    }

    public void setMaxTemp(int max) {
        if (ttvTemp != null)
            ttvTemp.setMaxTemp(max);
    }

    public void setMinTemp(int min) {
        if (ttvTemp != null) {
            ttvTemp.setMinTemp(min);
        }
    }
}
