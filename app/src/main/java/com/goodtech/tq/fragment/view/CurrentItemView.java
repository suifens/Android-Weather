package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.viewholder.HeaderItemsView;
import com.goodtech.tq.listener.WeatherHeaderListener;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.WeatherUtils;


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class CurrentItemView extends ConstraintLayout {

    public CurrentItemView(Context context) {
        this(context, null);
    }

    public CurrentItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CurrentItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    //天气图标
    public ImageView mIconImgV;
    //风向 等级 | 湿度 %
    public TextView mWind_rh;
    //温度
    public TextView mTempTv;
    //天气状态
    public TextView mPhraseTv;
    //提醒
    // public TextView mNotice;
    public HeaderItemsView mItemsView;

    private WeatherHeaderListener mListener;
    // public View mSignTipV;

    @SuppressLint("DefaultLocale")
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_current, this, true);
        mIconImgV = view.findViewById(R.id.img_icon);
        mWind_rh = view.findViewById(R.id.tv_rh_wrap);
        mTempTv = view.findViewById(R.id.tv_temperature);
        mPhraseTv = view.findViewById(R.id.tv_wx_phrase);
        mItemsView = view.findViewById(R.id.view_items);
        // 打车
        view.findViewById(R.id.btn_dache).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTaxi();
            }
        });

        view.findViewById(R.id.btn_meituan).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onMeituan();
            }
        });

        //
        view.findViewById(R.id.btn_eleme).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onEleme();
            }
        });

        // mSignTipV = view.findViewById(R.id.view_sign_tip);
    }

    /**
     * 设置回调
     */
    public void setItemListener(WeatherHeaderListener listener) {
        mListener = listener;
        if (mItemsView != null && listener != null) {
            mItemsView.setListener(listener);
        }
    }

    /**
     * 数据赋值
     */
    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model) {
        if (model != null) {
            setVisibility(VISIBLE);
            boolean hadSetTemp = false;
            String current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH");
            for (Hourly hourly : model.hourlies) {
                if (hourly != null) {
                    String dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH");
                    if (dayHour.equals(current)) {
                        mIconImgV.setImageResource(ImageUtils.weatherImageRes(hourly.icon_cd));
                        if (hourly.metric != null) {
                            mWind_rh.setText(String.format("%s风 %d级｜ 湿度%d%%", hourly.wdir_cardinal,
                                    WeatherUtils.windGrade(hourly.metric.wspd), hourly.rh));
                            mTempTv.setText(String.format("%d°", hourly.metric.temp));
                            hadSetTemp = true;
                        }
                        mPhraseTv.setText(hourly.phraseChar);
                    }
                }
            }

            if (model.observation != null) {
                Observation observation = model.observation;
                Metric metric = observation.metric;

                // Daily today = model.today();
                // if (today != null) {
                //     mNotice.setText(String.format("今天：当前%s，最高气温%dºC，最低气温%dºC", observation.wxPhrase,
                //             today.metric.maxTemp, today.metric.minTemp));
                // } else {
                //     mNotice.setText(String.format("今天：当前%s，最高气温%dºC，最低气温%dºC", observation.wxPhrase,
                //             metric.maxTemp, metric.minTemp));
                // }

                if (!hadSetTemp) {
                    mWind_rh.setText(String.format("%s风 %d级｜ 湿度%d%%", observation.wdirCardinal,
                            WeatherUtils.windGrade(metric.wspd), observation.rh));
                    mIconImgV.setImageResource(ImageUtils.weatherImageRes(observation.wxIcon));
                    mTempTv.setText(String.format("%d°", metric.temp));
                    mPhraseTv.setText(observation.wxPhrase);
                }
            }

            findViewById(R.id.layout_data).setVisibility(VISIBLE);
            // findViewById(R.id.layout_notice).setVisibility(VISIBLE);
        }
    }

    /**
     * 设置签到状态
     * @param signedIn 是否已签到
     */
    // public void setSignedIn(boolean signedIn) {
        // if (signedIn) {
        //     mSignTipV.setVisibility(View.GONE);
        // } else {
        //     mSignTipV.setVisibility(View.VISIBLE);
        // }
    // }

}
