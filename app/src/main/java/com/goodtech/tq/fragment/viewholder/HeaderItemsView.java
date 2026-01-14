package com.goodtech.tq.fragment.viewholder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.listener.WeatherHeaderListener;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class HeaderItemsView extends LinearLayout {

    public HeaderItemsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeaderItemsView(Context context) {
        super(context);
        init();
    }

    public void setListener(WeatherHeaderListener listener) {

        findViewById(R.id.item_typhoon).setOnClickListener(v -> {
            if (listener != null) {
                listener.onTyphoon();
            }
        });

        findViewById(R.id.item_air_quality).setOnClickListener(v -> {
            if (listener != null) {
                listener.onAirQuality();
            }
        });

        findViewById(R.id.item_calendar).setOnClickListener(v -> {
            if (listener != null) {
                listener.onCalendar();
            }
        });

        findViewById(R.id.item_constellation).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFortune();
            }
        });

        // findViewById(R.id.item_calling).setOnClickListener(v -> {
        //     if (listener != null) {
        //         listener.onDyMovie();
        //     }
        // });

        findViewById(R.id.item_sign).setOnClickListener(v -> {
            if (listener != null) {
                listener.onSignIn();
            }
        });

        findViewById(R.id.item_muyu).setOnClickListener(v -> {
            if (listener != null) {
                listener.onMuyu();
            }
        });
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.weather_item_header, this, true);
    }

}
