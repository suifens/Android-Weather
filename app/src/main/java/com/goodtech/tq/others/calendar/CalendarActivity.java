package com.goodtech.tq.others.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.github.gzuliyujiang.wheelpicker.DatePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.impl.BirthdayFormatter;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.models.calendar.DayDetail;
import com.goodtech.tq.models.calendar.Holiday;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

public class CalendarActivity extends BaseActivity implements
        CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener {

    TextView mTextYearMonth;
    CalendarView mCalendarView;
    CalendarLayout mCalendarLayout;

    private View mYearMonthView;
    private View mDayDetailView;
    private RecyclerView mRecyclerView;
    private HolidayRecyclerAdapter mAdapter;
    private CalendarPresenter mPresenter = CalendarPresenter.getInstance();

    private int mYear;
    private int mMonth;
    private int mDay;

    private CalendarPicker mPicker;

    public static void show(Context context) {
        context.startActivity(new Intent(context, CalendarActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        View topBar = findViewById(R.id.layout_top_bar);
        configStationBar(topBar);
        topBar.findViewById(R.id.button_back).setOnClickListener(v -> finish());

        mTextYearMonth = topBar.findViewById(R.id.tv_bar_title);
        mYearMonthView = topBar.findViewById(R.id.layout_time);

        mRecyclerView = findViewById(R.id.linear_holidays);
        mAdapter = new HolidayRecyclerAdapter(this, mPresenter.mHolidayList);
        mRecyclerView.setAdapter(mAdapter);

        configHolidays();

        initView();
    }

    private boolean firstLoad = true;
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("Ac_Calendar");
        MobclickAgent.onResume(this);
        if (firstLoad) {
            firstLoad = false;
            mHandler.postDelayed(() -> {
//                ((ViewStub) mDayDetailView).inflate();

                getDetails(TimeUtils.longToString(System.currentTimeMillis(), "yyyy-M-d"));
                updateHoliday(System.currentTimeMillis());

            }, 200);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Ac_Calendar");
        MobclickAgent.onPause(this);
    }

    /**
     * 更新假期
     */
    protected void updateHoliday(long timeMillis) {
        int year = TimeUtils.getYear(timeMillis);
        if (TimeUtils.afterHoliday(timeMillis)) {
            //  之后无假期，加载下一年的假期
            year += 1;
        }

        String yearStr = String.valueOf(year);
        ((TextView) findViewById(R.id.tv_year)).setText(yearStr);
        getHolidays(yearStr);

        // if (!TextUtils.isEmpty(mPresenter.mHolidayYear) && mPresenter.mHolidayYear.equals(yearStr)) {
        //     return;
        // }


    }

    /**
     * 配置station bar
     */
    @Override
    public void configStationBar(View stationBar) {
        LinearLayout.LayoutParams bars = new LinearLayout.LayoutParams(stationBar.getLayoutParams());
        bars.height = bars.height + DeviceUtils.getStatusBarHeight();
        stationBar.setLayoutParams(bars);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    protected void initView() {
        mCalendarView =  findViewById(R.id.calendarView);
        mDayDetailView = findViewById(R.id.viewStub_detail);
        mCalendarLayout = findViewById(R.id.calendarLayout);
        mCalendarView.setOnCalendarSelectListener(this);
        mCalendarView.setOnYearChangeListener(this);
        mYear = mCalendarView.getCurYear();
        mMonth = mCalendarView.getCurMonth();
        mDay = mCalendarView.getCurDay();
        mTextYearMonth.setText(mCalendarView.getCurYear() + "年" + mCalendarView.getCurMonth() + "月");

        mYearMonthView.setOnClickListener(v -> {

            if (mPicker != null && mPicker.isShowing()) {
                return;
            }
            CalendarPicker picker = new CalendarPicker(this);
            picker.setDefaultValue(mYear, mMonth, mDay);
            picker.setOnDatePickedListener((year, month, day) -> {
                mCalendarView.scrollToCalendar(year, month, day, false, true);
            });
            picker.show();
            mPicker = picker;

//            if (mCalendarView.isYearSelectLayoutVisible()) {
//                mCalendarView.closeYearSelectLayout();
//                return;
//            }
//
//            if (!mCalendarLayout.isExpand()) {
//                mCalendarLayout.expand();
//                return;
//            }
//            mCalendarView.showYearSelectLayout(mYear);
//            mTextYearMonth.setText(String.format("%d年", mYear));
        });
    }

    @Override
    public void onCalendarOutOfRange(Calendar calendar) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        mTextYearMonth.setVisibility(View.VISIBLE);
        mTextYearMonth.setText(calendar.getYear() + "年" + calendar.getMonth() + "月");
        updateHoliday(calendar.getTimeInMillis());
        mYear = calendar.getYear();
        mMonth = calendar.getMonth();
        mDay = calendar.getDay();

        getDetails(TimeUtils.longToString(calendar.getTimeInMillis(), "yyyy-M-d"));

        Log.e("onDateSelected", "  -- " + calendar.getYear() +
                "  --  " + calendar.getMonth() +
                "  -- " + calendar.getDay() +
                "  --  " + isClick + "  --   " + calendar.getScheme());
    }

    @Override
    public void onYearChange(int year) {
        mTextYearMonth.setText(year + "年");
    }

    private void getDetails(String day) {
        mPresenter.getDayDetails(day, () -> configDayDetail(mPresenter.mDayDetail));
    }

    @SuppressLint("SetTextI18n")
    private void configDayDetail(DayDetail dayDetail) {
        if (dayDetail == null) {
            return;
        }

        mHandler.post(() -> {
            int weekOfYear = TimeUtils.getYearWeek(TimeUtils.stringToDate(dayDetail.getDate(), "yyyy-M-d"));
            ((TextView) findViewById(R.id.tv_detail_week)).setText("第"+weekOfYear+"周" + " " + dayDetail.getWeekday());
            ((TextView) findViewById(R.id.tv_detail_lunar)).setText(dayDetail.getLunar());
            ((TextView) findViewById(R.id.tv_detail_suit)).setText(dayDetail.getSuit());
            ((TextView) findViewById(R.id.tv_detail_avoid)).setText(dayDetail.getAvoid());
        });
    }

    private void getHolidays(String year) {
        mPresenter.getHolidays(year, this::configHolidays);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void configHolidays() {
        mHandler.post(() -> {
            mAdapter.notifyDataSetChanged(mPresenter.mHolidayList);
            if (mPresenter.mHolidayList != null) {
                Map<String, Calendar> map = new HashMap<>();
                for (Holiday holiday : mPresenter.mHolidayList) {
                    for (Holiday.ListDay day : holiday.getList()) {
                        long date = TimeUtils.longWithDate(day.getDate(), "yyyy-M-d");
                        map.put(getSchemeCalendar(date, 0xFF40db25, "").toString(),
                                getSchemeCalendar(date, 0xFF40db25, ""));
                    }
                }
                mCalendarView.setSchemeDate(map);
            }
        });
    }

    private Calendar getSchemeCalendar(long date, int color, String text) {
        Calendar calendar = new Calendar();
        calendar.setYear(TimeUtils.getYear(date));
        calendar.setMonth(TimeUtils.getMonth(date));
        calendar.setDay(TimeUtils.getDay(date));
        calendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        calendar.setScheme(text);
        return calendar;
    }

    public static class CalendarPicker extends DatePicker {
        private static final int MAX_AGE = 50;

        public CalendarPicker(@NonNull Activity activity) {
            super(activity);
        }

        public CalendarPicker(@NonNull Activity activity, @StyleRes int themeResId) {
            super(activity, themeResId);
        }

        @Override
        protected void initData() {
            super.initData();
            titleView.setText("选择日期");
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int currentYear = calendar.get(java.util.Calendar.YEAR);
            DateEntity startValue = DateEntity.target(currentYear - MAX_AGE, 1, 1);
            DateEntity endValue = DateEntity.target(currentYear + MAX_AGE, 12, 12);
            wheelLayout.setRange(startValue, endValue);
            wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
            wheelLayout.setDateFormatter(new BirthdayFormatter());
        }

        public void setDefaultValue(int year, int month, int day) {
            wheelLayout.setDefaultValue(DateEntity.target(year, month, day));
        }

    }


}