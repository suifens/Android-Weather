package com.goodtech.tq.fragment.viewholder;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.listener.WeatherHeaderListener;
import com.goodtech.tq.utils.DeviceUtils;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class HeaderHolder extends RecyclerView.ViewHolder {

//    public HeaderHolder(View view, final WeatherHeaderListener listener) {
//        super(view);
//
//        view.findViewById(R.id.item_typhoon).setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onTyphoon();
//            }
//        });
//
//        view.findViewById(R.id.item_air_quality).setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onAirQuality();
//            }
//        });
//
//        view.findViewById(R.id.item_calendar).setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onCalendar();
//            }
//        });
//
//        view.findViewById(R.id.item_constellation).setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onFortune();
//            }
//        });
//    }

    public HeaderHolder(View view) {
        super(view);
        ConstraintLayout.LayoutParams bars = new ConstraintLayout.LayoutParams(view.getLayoutParams());
        bars.height = DeviceUtils.dip2px(view.getContext(), 50) + DeviceUtils.getStatusBarHeight();
        view.setLayoutParams(bars);
    }

    public static int getResource() {
        return R.layout.layout_empty;
    }

}
