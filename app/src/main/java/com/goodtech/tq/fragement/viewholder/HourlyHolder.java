package com.goodtech.tq.fragement.viewholder;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Hourly;

import java.util.List;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class HourlyHolder extends RecyclerView.ViewHolder {

    private RecyclerView mRecyclerView;

    public HourlyHolder(View view, Context context) {
        super(view);
        mRecyclerView = view.findViewById(R.id.list_hours);
    }

    public void setHourlies(List<Hourly> hourlies) {

    }

}
