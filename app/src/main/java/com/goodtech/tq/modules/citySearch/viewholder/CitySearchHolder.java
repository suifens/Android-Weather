package com.goodtech.tq.modules.citySearch.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import com.amap.api.services.core.PoiItemV2;
import com.goodtech.tq.R;
import com.goodtech.tq.modules.citySearch.CityRecyclerAdapter;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class CitySearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CityRecyclerAdapter.OnItemClickListener mListener;

    private final TextView mCityNameTv;
    private final TextView mAddressTv;
    private PoiItemV2 mCityMode;

    public CitySearchHolder(View view, CityRecyclerAdapter.OnItemClickListener listener) {
        super(view);
        mCityNameTv = view.findViewById(R.id.cityNameTv);
        mAddressTv = view.findViewById(R.id.addressTv);
        mListener = listener;
        view.setOnClickListener(this);
    }

    public static int resource() {
        return R.layout.search_item_city;
    }

    @SuppressLint("SetTextI18n")
    public void setCityMode(PoiItemV2 mode) {

        this.mCityMode = mode;
        mCityNameTv.setText(mode.getTitle());
        mAddressTv.setText(mode.getProvinceName()
                + " " + mode.getCityName()
                + " " + mode.getAdName()
                + " " + mode.getSnippet());
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClick(v, getAdapterPosition(), mCityMode);
        }
    }
}
