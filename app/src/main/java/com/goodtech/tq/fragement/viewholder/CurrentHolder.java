package com.goodtech.tq.fragement.viewholder;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodtech.tq.R;
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

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model) {
        if (model != null && model.observation != null) {
            Observation observation = model.observation;
            Metric metric = observation.metric;
            mWind_rh.setText(String.format("%s %d级｜ 湿度%d%%", observation.wdirCardinal, WeatherUtils.windGrade(metric.wspd), observation.rh));
            mIconImgV.setImageResource(ImageUtils.weatherImageRes(model.icon_cd));
            mTempTv.setText(String.format("%d", metric.temp));
            mPhraseTv.setText(observation.wxPhrase);
            mNotice.setText(String.format("今天：%s，最高气温 %dºC，最低气温 %dºC", observation.wxPhrase, metric.maxTemp, metric.minTemp));
        }
    }

}
