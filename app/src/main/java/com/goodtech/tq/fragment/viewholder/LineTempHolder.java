package com.goodtech.tq.fragment.viewholder;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.WeatherUtils;
import com.goodtech.tq.widget.weatherview.WeatherView;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class LineTempHolder extends RecyclerView.ViewHolder {

    public WeatherView weatherView;

    public LineTempHolder(View view) {
        super(view);
        weatherView = view.findViewById(R.id.weather_view);

        //画折线
        weatherView.setLineType(WeatherView.LINE_TYPE_DISCOUNT);
        //画曲线(已修复不圆滑问题)
//        weatherView.setLineType(WeatherView.LINE_TYPE_CURVE);

        //设置线宽
        weatherView.setLineWidth(2f);

        //设置一屏幕显示几列(最少3列)
        try {
            weatherView.setColumnNumber(6);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //设置白天和晚上线条的颜色
        weatherView.setDayAndNightLineColor(Color.parseColor("#FFD34E"), Color.parseColor("#00C4FF"));
    }

    public static int getResource() {
        return R.layout.weather_item_line;
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model) {
        if (model != null && model.dailies != null) {
            //填充天气数据
            weatherView.setList(model.dailies);
        }
    }

}
