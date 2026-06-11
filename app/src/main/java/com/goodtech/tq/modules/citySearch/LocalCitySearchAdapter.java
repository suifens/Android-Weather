package com.goodtech.tq.modules.citySearch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.CityMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地 city.db 全国城市搜索结果列表
 */
public class LocalCitySearchAdapter extends RecyclerView.Adapter<LocalCitySearchAdapter.Holder> {

    public interface OnItemClickListener {
        void onItemClick(CityMode city);
    }

    private final List<CityMode> cities = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void update(List<CityMode> list) {
        cities.clear();
        if (list != null) {
            cities.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item_city, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(cities.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final TextView nameTv;
        private final TextView addressTv;

        Holder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.cityNameTv);
            addressTv = itemView.findViewById(R.id.addressTv);
        }

        void bind(CityMode city, OnItemClickListener listener) {
            nameTv.setText(city.getMergerName());
            String sub = city.getCity();
            if (city.getPinyin() != null && !city.getPinyin().isEmpty()) {
                sub = sub + " · " + city.getPinyin();
            }
            addressTv.setText(sub);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(city);
                }
            });
        }
    }
}
