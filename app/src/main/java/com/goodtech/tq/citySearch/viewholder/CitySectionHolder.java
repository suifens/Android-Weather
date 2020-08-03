package com.goodtech.tq.citySearch.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.R;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class CitySectionHolder extends RecyclerView.ViewHolder {

    public TextView mTitleTv;

    public CitySectionHolder(View view) {
        super(view);
        mTitleTv = view.findViewById(R.id.tv_section_title);
    }

    public static int resource() {
        return R.layout.search_item_section;
    }

}
