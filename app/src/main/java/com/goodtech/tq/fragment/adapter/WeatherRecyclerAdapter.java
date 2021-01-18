package com.goodtech.tq.fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.fragment.viewholder.BottomHolder;
import com.goodtech.tq.fragment.viewholder.CurrentHolder;
import com.goodtech.tq.fragment.viewholder.DailyHolder;
import com.goodtech.tq.fragment.viewholder.HoursHolder;
import com.goodtech.tq.fragment.viewholder.LineTempHolder;
import com.goodtech.tq.fragment.viewholder.ObservationHolder;
import com.goodtech.tq.fragment.viewholder.RecentHolder;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.WeatherModel;

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder > {

    private final int CURRENT_VIEW = 0;
    private final int RECENT_VIEW = 1;
    private final int HOURS_VIEW = 2;
    private final int DAILY_VIEW = 3;
    private final int LINE_VIEW = 4;
    private final int OBSERVANT_VIEW = 5;
    private final int BOTTOM_VIEW = 6;

    private WeatherModel mModel;
    private final LayoutInflater mInflater;
    private String mAddress;

    public WeatherRecyclerAdapter(Context context, WeatherModel model, String address) {
        this.mInflater = LayoutInflater.from(context);
        this.mModel = model;
        this.mAddress = address;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public int getItemCount() {
        if (mModel != null) {
            return 14;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return CURRENT_VIEW;
            case 1:
                return RECENT_VIEW;
            case 2:
                return HOURS_VIEW;
            case 12:
                return LINE_VIEW;
            case 13:
                return OBSERVANT_VIEW;
            default:
                return DAILY_VIEW;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case CURRENT_VIEW:
                View current = getInflater().inflate(CurrentHolder.getResource(), parent, false);
                return new CurrentHolder(current);

            case RECENT_VIEW:
                View recentView = getInflater().inflate(RecentHolder.gerResource(), parent, false);
                return new RecentHolder(recentView);
            case HOURS_VIEW:
                View hoursView = getInflater().inflate(HoursHolder.getResource(), parent, false);
                return new HoursHolder(hoursView);
            case LINE_VIEW:
                View lineView = getInflater().inflate(LineTempHolder.getResource(), parent, false);
                return new LineTempHolder(lineView);
            case OBSERVANT_VIEW:
                View observationView = getInflater().inflate(ObservationHolder.getResource(), parent, false);
                return new ObservationHolder(observationView);
            case BOTTOM_VIEW:
                View bottomView = getInflater().inflate(BottomHolder.getResource(), parent, false);
                return new BottomHolder(bottomView);
            default:
                View dailyView = getInflater().inflate(DailyHolder.getResource(), parent, false);
                return new DailyHolder(dailyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (mModel != null) {
            if (viewHolder instanceof CurrentHolder) {
                ((CurrentHolder) viewHolder).setData(mModel);

            } else if (viewHolder instanceof RecentHolder) {
                ((RecentHolder) viewHolder).setData(mModel);

            } else if (viewHolder instanceof HoursHolder) {
                if (mModel.hourlies != null) {
                    ((HoursHolder) viewHolder).setHourlies(mModel);
                }

            } else if (viewHolder instanceof LineTempHolder) {
                if (mModel.dailies != null) {
                    ((LineTempHolder) viewHolder).setData(mModel);
                }
            } else if (viewHolder instanceof DailyHolder && i >= 3) {
                int index = i - 3;
                if (mModel.dailies != null && mModel.dailies.size() > index) {
                    Daily daily = mModel.dailies.get(index);
                    ((DailyHolder) viewHolder).setData(mModel, daily);
                }
            } else if (viewHolder instanceof ObservationHolder) {
                ((ObservationHolder) viewHolder).setData(mModel, mAddress);
            }
        }
    }

    public void notifyDataSetChanged(WeatherModel model, String address) {
        this.mModel = model;
        this.mAddress = address;
        super.notifyDataSetChanged();
    }
}
