package com.goodtech.tq.citySearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goodtech.tq.citySearch.viewholder.CityRecommendHolder;
import com.goodtech.tq.citySearch.viewholder.RecommendHeaderHolder;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;

import java.util.ArrayList;

public class CityRecommendAdapter extends Adapter {

    private final int HEADER_VIEW = 1;
    private final int RECOMMEND_VIEW = 2;

    private Context mContext;
    private WeatherModel mModel;
    private LayoutInflater mInflater;

    private ArrayList<CityMode> mCityList;
    private SearchCityType mType;
    private boolean mHadLocation;

    public CityRecommendAdapter(Context context, ArrayList<CityMode> cityModeArrayList) {
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

    @Override
    public int getItemViewType(int position) {
//        if (position == 0) {
//            return HEADER_VIEW;
//        }
        return RECOMMEND_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        if (viewType == HEADER_VIEW) {
//            View section = getInflater().inflate(RecommendHeaderHolder.resource(), parent, false);
//            return new RecommendHeaderHolder(section, mOnItemClickListener);
//        } else {
            View recommendView = getInflater().inflate(CityRecommendHolder.resource(), parent, false);
            return new CityRecommendHolder(recommendView, mOnItemClickListener);
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (mCityList != null && mCityList.size() > i) {
            CityMode mode = mCityList.get(i);
            if (viewHolder instanceof CityRecommendHolder) {
                ((CityRecommendHolder) viewHolder).setCityMode(mode);
            }
        }
    }

    public void notifyDataSetChanged(ArrayList<CityMode> cityModeArrayList, boolean location) {
        this.mCityList = cityModeArrayList;
        this.mHadLocation = location;
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
