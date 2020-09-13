package com.goodtech.tq.fragement.viewholder;

import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.utils.DeviceUtils;

/**
 * com.goodtech.tq.fragement.viewholder
 */
public class BottomHolder extends RecyclerView.ViewHolder {

    public BottomHolder(View view) {
        super(view);

        LinearLayout.LayoutParams bars = new LinearLayout.LayoutParams(view.getLayoutParams());
        bars.height = bars.height + DeviceUtils.getNavigationBarHeight();
        view.setLayoutParams(bars);
    }

    public static int getResource() {
        return R.layout.bottom_list;
    }

}
