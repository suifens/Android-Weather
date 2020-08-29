package com.goodtech.tq.cityList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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


public class CityListRecyclerAdapter extends RecyclerView.Adapter<CityListRecyclerAdapter.CityHolder>
        implements DraggableItemAdapter<CityListRecyclerAdapter.CityHolder> {

    private static final String TAG = "CityListRecyclerAdapter";

    private Context mContext;
    private WeatherModel mModel;
    private LayoutInflater mInflater;
    private CityListProvider mProvider;
    private boolean isEdit;

    private SearchCityType mType;

    public CityListRecyclerAdapter(Context context, CityListProvider provider) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mProvider = provider;

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
        return new CityHolder(mContext, section, mOnItemClickListener);
    }

    public void notifyDataSetChanged(boolean isEdit) {
        this.isEdit = isEdit;
        super.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull CityHolder viewHolder, int i) {

        final CityMode item = mProvider.getItem(i);
        if (item.cid != 0) {
            WeatherModel weatherModel = WeatherSpHelper.getWeatherModel(item.cid);
            viewHolder.setCityMode(item, weatherModel, isEdit);
        } else {
            viewHolder.setCityMode(item, null, isEdit);
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
        void onShowDelete(CityHolder holder);
        void onDeleteCity(int position, CityMode cityMode);
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
        private Context mContext;
        private OnItemClickListener mListener;
        private TextView mCityNameTv;
        private CityMode mCityMode;
        private LinearLayout mWeatherLayout;
        private ImageView mWeatherIcon;
        private TextView mTempTv;
        private ImageView mLocationTip;
        public RelativeLayout mContainer;
        public View mDragHandle;
        public ImageView mDeleteBtn;

        private boolean isEdit;

        public CityHolder(Context context, View view, OnItemClickListener listener) {
            super(view);
            mContext = context;
            mContainer = view.findViewById(R.id.container);
            mContainer.setOnClickListener(this);
            mDragHandle = view.findViewById(R.id.img_city_drag);
            mDeleteBtn = view.findViewById(R.id.img_delete);
            mDeleteBtn.setOnClickListener(this);
            mCityNameTv = view.findViewById(R.id.tv_city_name);
            mLocationTip = view.findViewById(R.id.img_location);
            mWeatherLayout = view.findViewById(R.id.layout_weather);
            mWeatherIcon = view.findViewById(R.id.img_icon);
            mTempTv = view.findViewById(R.id.tv_temperature);
            mListener = listener;
            view.findViewById(R.id.btn_delete).setOnClickListener(this);
            setAnimations();
        }

        @SuppressLint("DefaultLocale")
        public void setCityMode(CityMode mode, WeatherModel weatherModel, boolean isEdit) {
            this.isEdit = isEdit;
            this.mCityMode = mode;

            if (mode.location) {
                if (TextUtils.isEmpty(mode.city)) {
                    mCityNameTv.setText("定位");
                } else {
                    mCityNameTv.setText(mode.city);
                }
                mLocationTip.setVisibility(View.VISIBLE);
            } else {
                mCityNameTv.setText(mode.city);
                mLocationTip.setVisibility(View.GONE);
                if (isEdit) {
                    mDeleteBtn.setVisibility(View.VISIBLE);
                    mDragHandle.setVisibility(View.VISIBLE);
                    mWeatherLayout.setVisibility(View.GONE);
                    mContainer.setOnClickListener(null);
                } else {
                    mDeleteBtn.setVisibility(View.GONE);
                    mDragHandle.setVisibility(View.GONE);
                    mWeatherLayout.setVisibility(View.VISIBLE);
                    mContainer.setOnClickListener(this);
                }
            }

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
            switch (v.getId()) {
                case R.id.img_delete:
                    showDeleteAnim();
                    mContainer.setOnClickListener(this);
                    if (mListener != null) {
                        mListener.onShowDelete(this);
                    }
                    break;
                case R.id.container:
                    if (isEdit) {
                        hideDeleteAnim();
                        mContainer.setOnClickListener(null);
                        if (mListener != null) {
                            mListener.onShowDelete(null);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onItemClick(v, getAdapterPosition(), mCityMode);
                        }
                    }
                    break;
                case R.id.btn_delete:
                    if (mListener != null) {
                        mListener.onDeleteCity(getAdapterPosition(), mCityMode);
                    }
                    break;
            }
        }

        public void showDeleteAnim() {
            mContainer.startAnimation(showAnim);
        }
        
        public void hideDeleteAnim() {
            mContainer.startAnimation(hideAnim);
        }

        private AnimationSet showAnim;
        private AnimationSet hideAnim;

        private void setAnimations() {
            showAnim = getAnimation(R.anim.show_delete, false);
            hideAnim = getAnimation(R.anim.hide_delete, true);
        }

        public AnimationSet getAnimation(int animId, final boolean hide) {
            AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(mContext, animId);
            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    int left = hide ? 0 : -10;
                    int right = hide ? 0 : 210;
                    ll.setMargins(left, 0, right, 0);
                    mContainer.setLayoutParams(ll);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            return animationSet;
        }
    }
}
