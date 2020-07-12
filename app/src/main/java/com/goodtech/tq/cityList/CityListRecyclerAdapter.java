package com.goodtech.tq.cityList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.SearchCityType;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;

import java.util.ArrayList;

public class CityListRecyclerAdapter extends Adapter {

    private Context mContext;
    private WeatherModel mModel;
    private LayoutInflater mInflater;

    private ArrayList<CityMode> mCityList;
    private SearchCityType mType;

    public CityListRecyclerAdapter(Context context, ArrayList<CityMode> cityModeArrayList) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mCityList = cityModeArrayList;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public int getItemCount() {
        return mCityList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View section = getInflater().inflate(R.layout.item_city, parent, false);
        return new CityHolder(section, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (mCityList != null && mCityList.size() > i) {
            CityMode mode = mCityList.get(i);
            if (mode.cid != 0) {
                WeatherModel weatherModel = WeatherSpHelper.getWeatherModel(mode.cid);
                ((CityHolder) viewHolder).setCityMode(mode, weatherModel);
            } else {
                ((CityHolder) viewHolder).setCityMode(mode, null);
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


    /**
     * holder
     */
    public static class CityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnItemClickListener mListener;
        private TextView mCityNameTv;
        private CityMode mCityMode;
        private ImageView mWeatherIcon;
        private TextView mTempTv;
        private ImageView mLocationTip;

        public CityHolder(View view, OnItemClickListener listener) {
            super(view);
            mCityNameTv = view.findViewById(R.id.tv_city_name);
            mLocationTip = view.findViewById(R.id.img_location);
            mWeatherIcon = view.findViewById(R.id.img_icon);
            mTempTv = view.findViewById(R.id.tv_temperature);
            mListener = listener;
            view.setOnClickListener(this);
        }

        @SuppressLint("DefaultLocale")
        public void setCityMode(CityMode mode, WeatherModel weatherModel) {
            this.mCityMode = mode;

            if (mode.location && TextUtils.isEmpty(mode.city)) {
                mCityNameTv.setText("定位");
            }  else {
                mCityNameTv.setText(mode.city);
            }

            mLocationTip.setVisibility(mode.location ? View.VISIBLE : View.GONE);

            if (weatherModel != null && weatherModel.observation != null) {
                mWeatherIcon.setVisibility(View.VISIBLE);
                mWeatherIcon.setImageResource(ImageUtils.weatherImageRes(weatherModel.observation.wxIcon));
                if (weatherModel.observation != null && weatherModel.observation.metric != null) {
                    mTempTv.setText(String.format("%d℃", weatherModel.observation.metric.temp));
                }
                mTempTv.setTextColor(ContextCompat.getColor(mTempTv.getContext(), R.color.color_00c4ff));
            } else {
                mWeatherIcon.setVisibility(View.GONE);
                mTempTv.setText("数据更新中");
                mTempTv.setTextColor(ContextCompat.getColor(mTempTv.getContext(), R.color.color_4a4a4a));
            }
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition(), mCityMode);
            }
        }
    }
}
