package com.goodtech.tq.fragement.viewholder;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.views.ObservationItemView;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class ObservationHolder extends RecyclerView.ViewHolder {

    TextView mAddressTv;
    TextView mTempTv;
    TextView mSunriseTimeTv;
    TextView mSunsetTimeTv;

    ObservationItemView mTempItemView;
    ObservationItemView mWspdItemView;  //风速
    ObservationItemView mRhItemView;    //湿度
    ObservationItemView mDewptItemView; //露点
    ObservationItemView mPressureItemView; //气压
    ObservationItemView mUvItemView;    //紫外线
    ObservationItemView mVisibilityItemView; //能见度
    ObservationItemView mMoonItemView;  //月相

    public ObservationHolder(View view) {
        super(view);

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

    public static int getResource() {
        return R.layout.weather_item_observation;
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
