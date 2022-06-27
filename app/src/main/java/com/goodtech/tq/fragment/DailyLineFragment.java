package com.goodtech.tq.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodtech.tq.R;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.widget.weatherview.WeatherView;

public class DailyLineFragment extends BaseFragment {

    private WeatherView weatherView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_daily_line, container, false);
        weatherView = view.findViewById(R.id.weather_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //画折线
//        weatherView.setLineType(WeatherView.LINE_TYPE_DISCOUNT);
        //画曲线(已修复不圆滑问题)
        weatherView.setLineType(WeatherView.LINE_TYPE_CURVE);

        //设置线宽
        weatherView.setLineWidth(2f);

        //设置一屏幕显示几列(最少3列)
        try {
            weatherView.setColumnNumber(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //设置白天和晚上线条的颜色
        weatherView.setDayAndNightLineColor(Color.parseColor("#FFD34E"), Color.parseColor("#00C4FF"));

    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model) {
        if (model != null && model.dailies != null) {
            //填充天气数据
            weatherView.setList(model.dailies);

            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    weatherView.invalidate(), 100
            );
        }
    }
}
