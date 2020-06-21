package com.goodtech.tq.citySearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goodtech.tq.citySearch.viewholder.CityLocationHolder;
import com.goodtech.tq.citySearch.viewholder.CityRecommendHolder;
import com.goodtech.tq.citySearch.viewholder.CitySearchHolder;
import com.goodtech.tq.citySearch.viewholder.CitySectionHolder;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;

import java.util.ArrayList;

public class CityRecyclerAdapter extends Adapter {

    private final int TITLE_VIEW = 1;
    private final int LOCATION_VIEW = 2;
    private final int RECOMMEND_VIEW = 3;
    private final int SEARCH_VIEW = 4;

    private Context mContext;
    private WeatherModel mModel;
    private LayoutInflater mInflater;

    private ArrayList<CityMode> mCityList;
    private SearchCityType mType;
    private boolean mHadLocation = false;

    public CityRecyclerAdapter(Context context, ArrayList<CityMode> cityModeArrayList, SearchCityType type) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mCityList = cityModeArrayList;
        this.mType = type;
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
        if (mType == SearchCityType.Recommend) {
            switch (position) {
                case 0:
                case 2:
                    return TITLE_VIEW;
                case 1:
                    return LOCATION_VIEW;
                default:
                    return RECOMMEND_VIEW;
            }
        }
        return SEARCH_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mType == SearchCityType.Recommend) {
            switch (viewType) {
                case TITLE_VIEW:
                    View section = getInflater().inflate(CitySectionHolder.resource(), parent, false);
                    return new CitySectionHolder(section);

                case LOCATION_VIEW:
                    View locationView = getInflater().inflate(CityLocationHolder.resource(), parent, false);
                    return new CityLocationHolder(locationView, mOnItemClickListener);

                default:
                    View recommendView = getInflater().inflate(CityRecommendHolder.resource(), parent, false);
                    return new CityRecommendHolder(recommendView, mOnItemClickListener);
            }
        } else {
            View itemView = getInflater().inflate(CitySearchHolder.resource(), parent, false);
            return new CitySearchHolder(itemView, mOnItemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (mCityList != null && mCityList.size() > i) {
            CityMode mode = mCityList.get(i);

            if (viewHolder instanceof CitySectionHolder) {
                ((CitySectionHolder) viewHolder).mTitleTv.setText(mode.city);

            } else if (viewHolder instanceof CityLocationHolder) {
                ((CityLocationHolder) viewHolder).setCityMode(mode);

            } else if (viewHolder instanceof CityRecommendHolder) {
                ((CityRecommendHolder) viewHolder).setCityMode(mode);

            } else if (viewHolder instanceof CitySearchHolder) {
                ((CitySearchHolder) viewHolder).setCityMode(mode);
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
