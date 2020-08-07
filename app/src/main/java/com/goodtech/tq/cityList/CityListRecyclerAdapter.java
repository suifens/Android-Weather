package com.goodtech.tq.cityList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.SearchCityType;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.ImageUtils;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;


public class CityListRecyclerAdapter extends RecyclerView.Adapter<CityListRecyclerAdapter.CityHolder> implements DraggableItemAdapter<CityListRecyclerAdapter.CityHolder> {

    private static final String TAG = "CityListRecyclerAdapter";

    private Context mContext;
    private WeatherModel mModel;
    private LayoutInflater mInflater;
    private CityListProvider mProvider;

    private SearchCityType mType;

    public CityListRecyclerAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mProvider = new CityListProvider();

        setHasStableIds(true);
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public int getItemCount() {
        return mProvider.getCount();
    }

    @NonNull
    @Override
    public CityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View section = getInflater().inflate(R.layout.item_city, parent, false);
        return new CityHolder(section, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CityHolder viewHolder, int i) {

        final CityMode item = mProvider.getItem(i);
        if (item.cid != 0) {
            WeatherModel weatherModel = WeatherSpHelper.getWeatherModel(item.cid);
            viewHolder.setCityMode(item, weatherModel);
        } else {
            viewHolder.setCityMode(item, null);
        }

        // set background resource (target view ID: container)
        final DraggableItemState dragState = viewHolder.getDragState();

        if (dragState.isUpdated()) {
            int bgResId;

            if (dragState.isActive()) {
                bgResId = R.drawable.bg_item_dragging_active_state;

                // need to clear drawable state here to get correct appearance of the dragging item.
                DeviceUtils.clearState(viewHolder.mContainer.getForeground());
            } else if (dragState.isDragging()) {
                bgResId = R.drawable.bg_item_dragging_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            viewHolder.mContainer.setBackgroundResource(bgResId);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, CityMode cityMode);
    }

    private OnItemClickListener mOnItemClickListener;//声明接口

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return mProvider.getItem(position).getViewType();
    }

    @Override
    public boolean onCheckCanStartDrag(@NonNull CityHolder holder, int position, int x, int y) {

        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (containerView.getTranslationX() + 0.5f);
        final int offsetY = containerView.getTop() + (int) (containerView.getTranslationY() + 0.5f);

        return DeviceUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Nullable
    @Override
    public ItemDraggableRange onGetItemDraggableRange(@NonNull CityHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "onMoveItem: ");

        mProvider.moveItem(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public void onItemDragStarted(int position) {
        Log.d(TAG, "onItemDragStarted: ");
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        Log.d(TAG, "onItemDragFinished: ");
        notifyDataSetChanged();
    }

    /**
     * holder
     */
    public static class CityHolder extends AbstractDraggableItemViewHolder implements View.OnClickListener {
        private OnItemClickListener mListener;
        private TextView mCityNameTv;
        private CityMode mCityMode;
        private ImageView mWeatherIcon;
        private TextView mTempTv;
        private ImageView mLocationTip;
        public RelativeLayout mContainer;
        public View mDragHandle;

        public CityHolder(View view, OnItemClickListener listener) {
            super(view);
            mContainer = view.findViewById(R.id.container);
            mContainer.setOnClickListener(this);
            mDragHandle = view.findViewById(R.id.img_city_drag);
            mCityNameTv = view.findViewById(R.id.tv_city_name);
            mLocationTip = view.findViewById(R.id.img_location);
            mWeatherIcon = view.findViewById(R.id.img_icon);
            mTempTv = view.findViewById(R.id.tv_temperature);
            mListener = listener;
        }

        @SuppressLint("DefaultLocale")
        public void setCityMode(CityMode mode, WeatherModel weatherModel) {
            this.mCityMode = mode;

            if (mode.location && TextUtils.isEmpty(mode.city)) {
                mCityNameTv.setText("定位");
            } else {
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
