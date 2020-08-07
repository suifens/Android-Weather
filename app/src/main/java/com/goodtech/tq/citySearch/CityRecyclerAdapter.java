package com.goodtech.tq.citySearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.goodtech.tq.citySearch.viewholder.CitySearchHolder;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;

import java.util.ArrayList;

public class CityRecyclerAdapter extends Adapter {

    private Context mContext;
    private WeatherModel mModel;
    private LayoutInflater mInflater;

    private ArrayList<CityMode> mCityList;
    private boolean mHadLocation = false;

    public CityRecyclerAdapter(Context context, ArrayList<CityMode> cityModeArrayList) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mCityList = cityModeArrayList;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public int getItemCount() {
        if (mCityList == null) {
            return 0;
        } else {
            return mCityList.size();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = getInflater().inflate(CitySearchHolder.resource(), parent, false);
        return new CitySearchHolder(itemView, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (mCityList != null && mCityList.size() > i) {
            CityMode mode = mCityList.get(i);

            if (viewHolder instanceof CitySearchHolder) {
                ((CitySearchHolder) viewHolder).setCityMode(mode);
            }
        }
    }

    public void notifyDataSetChanged(ArrayList<CityMode> cityModeArrayList) {
        this.mCityList = cityModeArrayList;
        super.notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, CityMode cityMode);
    }

    private OnItemClickListener mOnItemClickListener;//声明接口

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
