package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.views.ObservationItemView;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class ObservationView extends LinearLayout {

    public ObservationView(Context context) {
        this(context, null);
    }

    public ObservationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ObservationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    private TextView mSunriseTimeTv;
    private TextView mSunsetTimeTv;
    private TextView mMinTempTv;
    private TextView mMaxTempTv;

    private ObservationItemView mWspdItemView;  //风速
    private ObservationItemView mRhItemView;    //湿度
    private ObservationItemView mDewptItemView; //露点
    private ObservationItemView mPressureItemView; //气压
    private ObservationItemView mUvItemView;    //紫外线
    private ObservationItemView mVisibilityItemView; //能见度

    @SuppressLint("DefaultLocale")
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_observation, this, true);
        mSunriseTimeTv = view.findViewById(R.id.tv_time_sunrise);
        mSunsetTimeTv = view.findViewById(R.id.tv_time_sunset);
        mMinTempTv = view.findViewById(R.id.tv_min_temp);
        mMaxTempTv = view.findViewById(R.id.tv_max_temp);

        mWspdItemView = view.findViewById(R.id.layout_wind_speed);
        mRhItemView = view.findViewById(R.id.layout_rh);
        mDewptItemView = view.findViewById(R.id.layout_dewpt);
        mPressureItemView = view.findViewById(R.id.layout_pressure);
        mUvItemView = view.findViewById(R.id.layout_uv_index);
        mVisibilityItemView = view.findViewById(R.id.layout_visibility);
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model) {
        if (model != null) {
            Observation observation = model.observation;
            if (observation == null) {
                return;
            }
            Metric metric = observation.metric;
            long current = System.currentTimeMillis();
            String currentStr = TimeUtils.longToString(current, "yyyy-MM-dd");
            String minTemp = null;
            String maxTemp = null;

            if (model.dailies != null) {
                Daily daily = null;
                for (int i = 0; i < model.dailies.size(); i++) {
                    Daily temp = model.dailies.get(i);
                    if (temp != null && temp.fcst_valid_local.contains(currentStr)) {
                        daily = temp;
                        break;
                    }
                }

                if (daily != null) {
                    String sunrise = TimeUtils.timeToHHmm(TimeUtils.switchTime(daily.sunRise));
                    String sunset = TimeUtils.timeToHHmm(TimeUtils.switchTime(daily.sunSet));
                    mSunriseTimeTv.setText(String.format("日出%s", sunrise));
                    mSunsetTimeTv.setText(String.format("日落%s", sunset));
                    minTemp = String.format("%d°C", daily.metric.minTemp);
                    maxTemp = String.format("%d°C", daily.metric.maxTemp);
                }
            }

            if (minTemp == null) {
                minTemp = String.format("%d°C", metric.minTemp);
                maxTemp = String.format("%d°C", metric.maxTemp);
            }
            mMinTempTv.setText(minTemp);
            mMaxTempTv.setText(maxTemp);
            mWspdItemView.setValue(String.format("%d", metric.wspd));
            mRhItemView.setValue(String.format("%d%%", observation.rh));
            mDewptItemView.setValue(String.format("%d°", metric.dewpt));
            mPressureItemView.setValue(String.format("%.1f", metric.pressure));
            mUvItemView.setValue(String.format("%d", observation.uvIndex));
            mVisibilityItemView.setValue(String.format("%.2f", metric.vis));

        }
    }

}
