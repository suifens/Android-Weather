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

    private TextView mAddressTv;
    private TextView mTempTv;
    private TextView mSunriseTimeTv;
    private TextView mSunsetTimeTv;

    private ObservationItemView mTempItemView;
    private ObservationItemView mWspdItemView;  //风速
    private ObservationItemView mRhItemView;    //湿度
    private ObservationItemView mDewptItemView; //露点
    private ObservationItemView mPressureItemView; //气压
    private ObservationItemView mUvItemView;    //紫外线
    private ObservationItemView mVisibilityItemView; //能见度
    private ObservationItemView mMoonItemView;  //月相
    
    @SuppressLint("DefaultLocale")
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_observation, this, true);
        mAddressTv = view.findViewById(R.id.tv_address);
        mTempTv = view.findViewById(R.id.tv_temperature);
        mSunriseTimeTv = view.findViewById(R.id.tv_time_sunrise);
        mSunsetTimeTv = view.findViewById(R.id.tv_time_sunset);

        mTempItemView = view.findViewById(R.id.layout_temperature);
        mWspdItemView = view.findViewById(R.id.layout_wind_speed);
        mRhItemView = view.findViewById(R.id.layout_rh);
        mDewptItemView = view.findViewById(R.id.layout_dewpt);
        mPressureItemView = view.findViewById(R.id.layout_pressure);
        mUvItemView = view.findViewById(R.id.layout_uv_index);
        mVisibilityItemView = view.findViewById(R.id.layout_visibility);
        mMoonItemView = view.findViewById(R.id.layout_moon_phase);
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model, String address) {
        if (model != null) {
            mAddressTv.setText(address);
            Observation observation = model.observation;
            if (observation == null) {
                return;
            }
            Metric metric = observation.metric;
            long current = System.currentTimeMillis();
            String currentStr = TimeUtils.longToString(current, "yyyy-MM-dd");
            String tempString = null;

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
                    mSunriseTimeTv.setText(sunrise);
                    mSunsetTimeTv.setText(sunset);
                    mMoonItemView.setValue(daily.moon_phase);
                    tempString = String.format("%d℃/%d℃", daily.metric.maxTemp, daily.metric.minTemp);
                }
            }

            mTempTv.setText(String.format("%d", metric.temp));
            if (tempString == null) {
                tempString = String.format("%d℃/%d℃", metric.maxTemp, metric.minTemp);
            }
            mTempItemView.setValue(tempString);
            mWspdItemView.setValue(String.format("%d公里/小时", metric.wspd));
            mRhItemView.setValue(String.format("%d%%", observation.rh));
            mDewptItemView.setValue(String.format("%d℃", metric.dewpt));
            mPressureItemView.setValue(String.format("%.1f毫巴", metric.pressure));
            mUvItemView.setValue(String.format("%d (最大值10)", observation.uvIndex));
            mVisibilityItemView.setValue(String.format("%.2f公里", metric.vis));

        }
    }

}
