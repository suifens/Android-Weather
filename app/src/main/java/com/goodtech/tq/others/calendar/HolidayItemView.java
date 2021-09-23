package com.goodtech.tq.others.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.calendar.Holiday;
import com.goodtech.tq.utils.TimeUtils;

/**
 * com.goodtech.tq.others.fortune
 */
public class HolidayItemView extends LinearLayout {

    private TextView mDayTv;
    private TextView mWeekTv;
    private TextView mNameTv;
    private TextView mHolidayTimeTv;
    private TextView mLengthTv;

    public HolidayItemView(Context context) {
        this(context, null);
    }

    public HolidayItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_calendar_holiday, this);

        mDayTv = findViewById(R.id.tv_day);
        mWeekTv = findViewById(R.id.tv_week);
        mNameTv = findViewById(R.id.tv_holiday_name);
        mHolidayTimeTv = findViewById(R.id.tv_holiday_time);
        mLengthTv = findViewById(R.id.tv_holiday_length);
    }

    @SuppressLint("DefaultLocale")
    public void setData(Holiday data) {
        mDayTv.setText(data.getDay());
        mWeekTv.setText(TimeUtils.getWeek(data.getFestival()));
        mNameTv.setText(data.getName());
        mHolidayTimeTv.setText(data.getTimeSpan());
        mLengthTv.setText(String.format("共%d天", data.getHoliday()));
    }
}
