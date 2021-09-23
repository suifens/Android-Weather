package com.goodtech.tq.others.calendar;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.listener.CompletionListener;
import com.goodtech.tq.models.calendar.DayDetail;
import com.goodtech.tq.models.calendar.Holiday;
import com.goodtech.tq.utils.SpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarPresenter {

    private static CalendarPresenter singleton;

    private CalendarPresenter() {}

    public static CalendarPresenter getInstance() {
        if (singleton == null) {
            singleton = new CalendarPresenter();
        }
        return singleton;
    }

    public DayDetail mDayDetail;

    public List<Holiday> mHolidayList;

    private Map<String, DayDetail> mDayDetails = new HashMap<>();

    private Map<String, List<Holiday>> mHolidayMap = new HashMap<>();

    private static final String TAG = "CalendarPresenter";
    public void getDayDetails(String day, CompletionListener callback) {

        if (mDayDetails.containsKey(day)) {
            mDayDetail = mDayDetails.get(day);
            if (callback != null) {
                callback.onCompletion();
            }
            return;
        }

        JuHeHelper.getInstance().fetchDayDetails(day, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (success) {
                        if (!jsonObject.isNull("result")) {
                            JSONObject data = jsonObject.getJSONObject("result").getJSONObject("data");
                            DayDetail model = new Gson().fromJson(String.valueOf(data), new TypeToken<DayDetail>() {
                            }.getType());
                            if (model != null) {
                                mDayDetail = model;
                                mDayDetails.put(day, model);
                            }
                            if (callback != null) {
                                callback.onCompletion();
                            }
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (callback != null) {
                    callback.onCompletion();
                }
            }
        });
    }

    public void getHolidays(String year, CompletionListener callback) {

        if (mHolidayMap.containsKey(year)) {
            mHolidayList = mHolidayMap.get(year);
            if (callback != null) {
                callback.onCompletion();
            }
            return;
        }

        String holidayJson = SpUtils.getInstance().getString(String.format("holiday-%s", year), "");
        if (!TextUtils.isEmpty(holidayJson)) {
            List<Holiday> list = new Gson().fromJson(holidayJson, new TypeToken<List<Holiday>>() {
            }.getType());
            if (list != null && list.size() > 0) {
                mHolidayList = list;
                mHolidayMap.put(year, list);
                if (callback != null) {
                    callback.onCompletion();
                }
                return;
            }
        }

        List<Holiday> holidayList = new ArrayList<>();
        final int[] count = {0};
        for (int i = 1; i <= 12; i++) {
            @SuppressLint("DefaultLocale") String yearMonth = String.format("%s-%d", year, i);
            JuHeHelper.getInstance().fetchMonthHoliday(yearMonth, new ApiResponseHandler() {
                @Override
                public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                    count[0]++;
                    try {
                        if (success) {
                            if (!jsonObject.isNull("result")) {
                                JSONObject data = jsonObject.getJSONObject("result").getJSONObject("data");
                                if (data != null) {
                                    JSONArray array = data.getJSONArray("holiday_array");
                                    List<Holiday> dataList = new Gson().fromJson(String.valueOf(array), new TypeToken<List<Holiday>>() {
                                    }.getType());
                                    if (dataList != null) {
                                        for (Holiday holiday : dataList) {
                                            boolean isContain = false;
                                            for (int j = holidayList.size() - 1; j >= 0 ; j--) {
                                                Holiday lastDay = holidayList.get(j);
                                                if (lastDay.getName().equals(holiday.getName())) {
                                                    isContain = true;
                                                    break;
                                                }
                                            }
                                            if (!isContain) {
                                                holidayList.add(holiday);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (count[0] == 12) {

                        Collections.sort(holidayList, (o1, o2) -> Long.compare(o1.getDate(), o2.getDate()));

                        if (holidayList.size() > 0) {
                            mHolidayList = holidayList;
                            mHolidayMap.put(year, holidayList);

                            SpUtils.getInstance().putString(String.format("holiday-%s", year), new Gson().toJson(holidayList));
                        }
                        if (callback != null) {
                            callback.onCompletion();
                        }
                    }
                }
            });
        }
    }

}
