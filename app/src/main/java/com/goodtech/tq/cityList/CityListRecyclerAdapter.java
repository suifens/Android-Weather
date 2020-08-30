package com.goodtech.tq.cityList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.viewholder.CityHolder;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.DeviceUtils;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;


public class CityListRecyclerAdapter extends RecyclerView.Adapter<CityHolder>
        implements DraggableItemAdapter<CityHolder> {

    private static final String TAG = "CityListRecyclerAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private CityListProvider mProvider;
    private boolean isEdit;

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
}
