package com.goodtech.tq.fragment.adapter;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoursRecyclerAdapter extends RecyclerView.Adapter<HoursRecyclerAdapter.HourlyHolder> {

    private List mList;

    public HoursRecyclerAdapter(List items) {
        mList = items;
    }

    public void notifyDataSetChanged(List<Parcelable> dataList) {
        this.mList = dataList;
        super.notifyDataSetChanged();
    }

    @NotNull
    @Override
    public HourlyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.constraint_item_hourly, parent, false);
        return new HourlyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final HourlyHolder holder, int position) {
        Hourly hourly = (Hourly) mList.get(position);
        holder.setData(hourly);
    }

    @Override
    public int getItemCount() {
        return this.mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    /**
     * item holder
     */
    static class HourlyHolder extends RecyclerView.ViewHolder {
        private TextView mHour;
        private ImageView mWeatherIcon;
        private TextView mTemperature;

        HourlyHolder(View view) {
            super(view);
            mHour = view.findViewById(R.id.tv_hour);
            mWeatherIcon = view.findViewById(R.id.img_icon);
            mTemperature = view.findViewById(R.id.tv_temperature);
        }

        @SuppressLint("DefaultLocale")
        void setData(Hourly hourly) {

            String hour = TimeUtils.timeToHH(hourly.fcst_valid * 1000);
            String dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH");
            String current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH");
            String timeStr;
            boolean sun = hourly.sunrise | hourly.sunset;
            
            if (dayHour.equals(current)) {
                timeStr = "现在";
            } else {
                timeStr = String.format("%s时", hour);
                if (sun) {
                    timeStr = TimeUtils.timeToHHmm(hourly.fcst_valid * 1000);
                }
            }

            mHour.setText(timeStr);
            if (sun) {
                if (hourly.sunrise) {
                    mWeatherIcon.setImageResource(R.drawable.ic_sunrise);
                    mTemperature.setText("日出");
                }
                if (hourly.sunset) {
                    mWeatherIcon.setImageResource(R.drawable.ic_sunset);
                    mTemperature.setText("日落");
                }
            } else {
                mWeatherIcon.setImageResource(ImageUtils.weatherImageRes(hourly.icon_cd));
                mTemperature.setText(String.format("%d℃", hourly.metric.temp));
            }
        }
    }
}
