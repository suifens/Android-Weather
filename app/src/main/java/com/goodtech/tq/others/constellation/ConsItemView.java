package com.goodtech.tq.others.constellation;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;

/**
 * com.goodtech.tq.others.fortune
 */
public class ConsItemView extends LinearLayout {

    private TextView mTitleTv;
    private TextView mDescriptionTv;

    public ConsItemView(Context context) {
        this(context, null);
    }

    public ConsItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_cons_item, this);

        mTitleTv = findViewById(R.id.tv_item_title);
        mDescriptionTv = findViewById(R.id.tv_item_detail);
    }

    public void setData(String title, String description) {
        if (TextUtils.isEmpty(title)) {
            mTitleTv.setVisibility(View.GONE);
        } else {
            mTitleTv.setText(String.format("[%s]", title));
        }
        mDescriptionTv.setText(description);
    }
}
