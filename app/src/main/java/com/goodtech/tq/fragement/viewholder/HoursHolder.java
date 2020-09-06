package com.goodtech.tq.fragement.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.goodtech.tq.R;
import com.goodtech.tq.fragement.adapter.HoursRecyclerAdapter;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class HoursHolder extends RecyclerView.ViewHolder {

    private HoursRecyclerAdapter mAdapter;

    public HoursHolder(View view) {
        super(view);
        RecyclerView mRecyclerView = view.findViewById(R.id.list_hours);
        mAdapter = new HoursRecyclerAdapter(new ArrayList());
        mRecyclerView.setAdapter(mAdapter);
    }

    public static int getResource() {
        return R.layout.weather_item_hours;
    }

    public void setHourlies(WeatherModel model) {
        if (model != null && model.hourlies != null) {
            ArrayList<Hourly> hourlies = new ArrayList<>();
            hourlies.addAll(model.hourlies);

            checkFirstHourly(model, hourlies);

            Daily daily = model.dailies.get(0);
            Daily tomorrow = model.dailies.get(1);
            if (daily != null) {

                long tSunrise = TimeUtils.switchTime(daily.sunRise);
                long tSunset = TimeUtils.switchTime(daily.sunSet);
                long mSunrise = TimeUtils.switchTime(tomorrow.sunRise);
                long mSunset = TimeUtils.switchTime(tomorrow.sunSet);
                long current = System.currentTimeMillis();

                String currentStr = TimeUtils.timeToHH(System.currentTimeMillis());
                String riseString = TimeUtils.timeToHH(tSunrise);
                String setString = TimeUtils.timeToHH(tSunset);

                long sunrise = current > tSunrise ? mSunrise : tSunrise;
                long sunset = current > tSunset ? mSunset : tSunset;

                boolean addRise = false;

                for (int i = 0; i < model.hourlies.size(); i++) {
                    Hourly hourly = model.hourlies.get(i);
                    if (currentStr.compareTo(riseString) != 0
                            && !addRise && sunrise < hourly.fcst_valid * 1000) {
                        addRise = true;
                        Hourly riseHourly = new Hourly();
                        riseHourly.fcst_valid = sunrise / 1000;
                        riseHourly.fcst_valid_local = daily.sunRise;
                        riseHourly.sunrise = true;
                        hourlies.add(i, riseHourly);
                        continue;
                    }
                    if (currentStr.compareTo(setString) != 0 && sunset < hourly.fcst_valid * 1000) {
                        Hourly setHourly = new Hourly();
                        setHourly.fcst_valid = sunset / 1000;
                        setHourly.fcst_valid_local = daily.sunSet;
                        setHourly.sunset = true;
                        hourlies.add(i + 1, setHourly);
                        break;
                    }
                }
            }

            setHourlies(hourlies);
        }
    }

    protected void checkFirstHourly(WeatherModel model, ArrayList<Hourly> hourlies) {
        if (model != null && hourlies.size() > 0) {
            Hourly hourly = hourlies.get(0);
            if (hourly.fcst_valid > System.currentTimeMillis() / 1000) {
                if (model.observation != null) {
                    Observation observation = model.observation;
                    Metric metric = observation.metric;
                    Hourly curHourly = new Hourly();
                    curHourly.fcst_valid = System.currentTimeMillis() / 1000;
                    curHourly.metric = metric;
                    curHourly.icon_cd = observation.wxIcon;
                    hourlies.add(0, curHourly);
                }
            }
        }
    }

    public void setHourlies(List hourlies) {
        mAdapter.notifyDataSetChanged(hourlies);
    }

}
