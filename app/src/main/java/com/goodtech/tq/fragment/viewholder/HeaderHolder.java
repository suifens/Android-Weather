package com.goodtech.tq.fragment.viewholder;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.listener.WeatherHeaderListener;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class HeaderHolder extends RecyclerView.ViewHolder {

    public HeaderHolder(View view, final WeatherHeaderListener listener) {
        super(view);

        view.findViewById(R.id.item_weekend_weather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onWeekendWeather();
                }
            }
        });

        view.findViewById(R.id.item_air_quality).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAirQuality();
                }
            }
        });

        view.findViewById(R.id.item_calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCalendar();
                }
            }
        });

        view.findViewById(R.id.item_constellation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFortune();
                }
            }
        });
    }

    public static int getResource() {
        return R.layout.weather_item_header;
    }

}
