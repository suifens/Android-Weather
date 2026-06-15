package com.goodtech.tq.modules.citySearch;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.models.CityMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 本地 city.db 全国城市搜索结果列表
 */
public class LocalCitySearchAdapter extends RecyclerView.Adapter<LocalCitySearchAdapter.Holder> {

    public interface OnItemClickListener {
        void onItemClick(CityMode city);
    }

    private final List<CityMode> cities = new ArrayList<>();
    private String searchKeyword = "";
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void update(List<CityMode> list, String keyword) {
        cities.clear();
        searchKeyword = keyword != null ? keyword.trim() : "";
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
        holder.bind(cities.get(position), searchKeyword, listener);
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

        void bind(CityMode city, String keyword, OnItemClickListener listener) {
            nameTv.setText(buildHighlightText(nameTv, city.getMergerName(), keyword));
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

        private CharSequence buildHighlightText(TextView textView, String text, String keyword) {
            if (TextUtils.isEmpty(text)) {
                return "";
            }
            if (TextUtils.isEmpty(keyword)) {
                return text;
            }
            String lowerText = text.toLowerCase(Locale.getDefault());
            String lowerKeyword = keyword.toLowerCase(Locale.getDefault());
            int index = lowerText.indexOf(lowerKeyword);
            if (index < 0) {
                return text;
            }
            SpannableString spannable = new SpannableString(text);
            @ColorInt int color = ContextCompat.getColor(textView.getContext(), R.color.color_orange);
            spannable.setSpan(
                    new ForegroundColorSpan(color),
                    index,
                    index + keyword.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return spannable;
        }
    }
}
