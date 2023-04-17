package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.adapter.HoursRecyclerAdapter;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class HoursItemView extends LinearLayout {

    public HoursItemView(Context context) {
        this(context, null);
    }

    public HoursItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public HoursItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    private HoursRecyclerAdapter mAdapter;
    
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_hours, this, true);
        RecyclerView recyclerView = view.findViewById(R.id.list_hours);
        mAdapter = new HoursRecyclerAdapter(new ArrayList());
        recyclerView.setAdapter(mAdapter);
    }

    public void setHourlies(WeatherModel model) {
        if (model != null && model.hourlies != null) {
            setVisibility(VISIBLE);
            ArrayList<Hourly> hourlies = new ArrayList<>();

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
                boolean addSet = false;

                for (int i = 0; i < model.hourlies.size(); i++) {
                    Hourly hourly = model.hourlies.get(i);
                    if (currentStr.compareTo(riseString) != 0
                            && !addRise && sunrise < hourly.fcst_valid * 1000) {
                        addRise = true;
                        Hourly riseHourly = new Hourly();
                        riseHourly.fcst_valid = sunrise / 1000;
                        riseHourly.fcst_valid_local = daily.sunRise;
                        riseHourly.sunrise = true;
                        hourlies.add(riseHourly);
                    }
                    if (currentStr.compareTo(setString) != 0
                            && !addSet
                            && sunset < hourly.fcst_valid * 1000) {
                        Hourly setHourly = new Hourly();
                        addSet = true;
                        setHourly.fcst_valid = sunset / 1000;
                        setHourly.fcst_valid_local = daily.sunSet;
                        setHourly.sunset = true;
                        hourlies.add(setHourly);
                    }
                    hourlies.add(hourly);
                }
            }

            setHourlies(hourlies);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setHourlies(List hourlies) {
        mAdapter.notifyDataSetChanged(hourlies);
    }

    protected void checkFirstHourly(WeatherModel model, ArrayList<Hourly> hourlies) {
        if (model != null && model.hourlies != null && model.hourlies.size() > 0) {
            Hourly hourly = model.hourlies.get(0);
            if (hourly.fcst_valid > System.currentTimeMillis() / 1000) {
                if (model.observation != null) {
                    Observation observation = model.observation;
                    Metric metric = observation.metric;
                    Hourly curHourly = new Hourly();
                    curHourly.fcst_valid = System.currentTimeMillis() / 1000;
                    curHourly.metric = metric;
                    curHourly.icon_cd = observation.wxIcon;
                    curHourly.phraseChar = observation.getWxPhrase();
                    hourlies.add(curHourly);
                }
            }
        }
    }
}
