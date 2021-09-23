package com.goodtech.tq.others.calendar.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Daypart;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.models.calendar.Holiday;
import com.goodtech.tq.utils.TimeUtils;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class HolidayHolder extends RecyclerView.ViewHolder {

    private TextView mDayTv;
    private TextView mWeekTv;
    private TextView mNameTv;
    private TextView mHolidayTimeTv;
    private TextView mLengthTv;
    private TextView mRestTv;

    public HolidayHolder(View view) {
        super(view);
        mDayTv = view.findViewById(R.id.tv_day);
        mWeekTv = view.findViewById(R.id.tv_week);
        mNameTv = view.findViewById(R.id.tv_holiday_name);
        mHolidayTimeTv = view.findViewById(R.id.tv_holiday_time);
        mLengthTv = view.findViewById(R.id.tv_holiday_length);
        mRestTv = view.findViewById(R.id.tv_holiday_rest);
    }

    public static int gerResource() {
        return R.layout.view_calendar_holiday;
    }

    @SuppressLint("DefaultLocale")
    public void setData(Holiday data) {
        mDayTv.setText(data.getDay());
        mWeekTv.setText(TimeUtils.getWeek(data.getFestival()));
        mNameTv.setText(data.getName());
        mHolidayTimeTv.setText(data.getTimeSpan());
        mLengthTv.setText(String.format("共%d天", data.getHoliday()));
        if (!TextUtils.isEmpty(data.getRest())) {
            mRestTv.setVisibility(View.VISIBLE);
            mRestTv.setText(data.getRest());
        } else {
            mRestTv.setVisibility(View.GONE);
        }
    }

}
