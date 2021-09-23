package com.goodtech.tq.others.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.models.calendar.Holiday;
import com.goodtech.tq.others.calendar.view.HolidayHolder;

import java.util.List;

public class HolidayRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder > {

    private List<Holiday> mHolidayList;
    private final LayoutInflater mInflater;
    private final Context mContext;

    public HolidayRecyclerAdapter(Context context, List<Holiday> holidayList) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mHolidayList = holidayList;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public int getItemCount() {
        if (mHolidayList == null) return 0;
        return mHolidayList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View header = getInflater().inflate(HolidayHolder.gerResource(), parent, false);
        return new HolidayHolder(header);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ((HolidayHolder)viewHolder).setData(mHolidayList.get(i));

    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataSetChanged(List<Holiday> list) {
        this.mHolidayList = list;
        super.notifyDataSetChanged();
    }
}
