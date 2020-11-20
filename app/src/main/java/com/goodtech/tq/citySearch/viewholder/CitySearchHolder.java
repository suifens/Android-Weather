package com.goodtech.tq.citySearch.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CityRecyclerAdapter;
import com.goodtech.tq.models.CityMode;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class CitySearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CityRecyclerAdapter.OnItemClickListener mListener;

    private TextView mCityTv;
    private CityMode mCityMode;

    public CitySearchHolder(View view, CityRecyclerAdapter.OnItemClickListener listener) {
        super(view);
        mCityTv = view.findViewById(R.id.tv_search_city);
        mListener = listener;
        view.setOnClickListener(this);
    }

    public static int resource() {
        return R.layout.search_item_city;
    }

    public void setCityMode(CityMode mode) {

        this.mCityMode = mode;
        String searchCity = String.format("%s", mode.mergerName);
        mCityTv.setText(searchCity);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClick(v, getAdapterPosition(), mCityMode);
        }
    }
}
