package com.goodtech.tq.fragement.viewholder;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.WeatherUtils;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class CurrentHolder extends RecyclerView.ViewHolder {

    //天气图标
    public ImageView mIconImgV;
    //风向 等级 | 湿度 %
    public TextView mWind_rh;
    //温度
    public TextView mTempTv;
    //天气状态
    public TextView mPhraseTv;
    //提醒
    public TextView mNotice;

    public CurrentHolder(View view) {
        super(view);
        mIconImgV = view.findViewById(R.id.img_icon);
        mWind_rh = view.findViewById(R.id.tv_rh_wrap);
        mTempTv = view.findViewById(R.id.tv_temperature);
        mPhraseTv = view.findViewById(R.id.tv_wx_phrase);
        mNotice = view.findViewById(R.id.tv_notice);
    }

    public static int getResource() {
        return R.layout.weather_item_current;
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model) {
        if (model != null && model.observation != null) {
            Observation observation = model.observation;
            Metric metric = observation.metric;
            mNotice.setText(String.format("今天：%s，最高气温 %dºC，最低气温 %dºC", observation.wxPhrase, metric.maxTemp, metric.minTemp));

            if (model.hourlies.size() > 0) {
                Hourly hourly = model.hourlies.get(0);
                if (hourly != null) {
                    long time = hourly.fcst_valid;
                    if (System.currentTimeMillis() > time * 1000) {
                        //
                        mIconImgV.setImageResource(ImageUtils.weatherImageRes(hourly.icon_cd));
                        if (hourly.metric != null) {
                            mWind_rh.setText(String.format("%s %d级｜ 湿度%d%%", hourly.wdir_cardinal,
                                    WeatherUtils.windGrade(hourly.metric.wspd), hourly.rh));
                            mTempTv.setText(String.format("%d", hourly.metric.temp));
                        }
                        mPhraseTv.setText(hourly.phraseChar);
                        return;
                    }
                }
            }
            mWind_rh.setText(String.format("%s %d级｜ 湿度%d%%", observation.wdirCardinal, WeatherUtils.windGrade(metric.wspd), observation.rh));
            mIconImgV.setImageResource(ImageUtils.weatherImageRes(observation.wxIcon));
            mTempTv.setText(String.format("%d", metric.temp));
            mPhraseTv.setText(observation.wxPhrase);

        }
    }

}
