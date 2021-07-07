package com.goodtech.tq.others.airQuality.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;

public class AirPmItemView extends ConstraintLayout {

    public AirPmItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AirPmItemView(Context context) {
        super(context);
        init();
    }

    private TextView mTitleTv;
    private TextView mValueTv;
    private View mTipView;

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.air_stub_pm_item, this, true);
        mTitleTv = findViewById(R.id.tv_air_pm_title);
        mValueTv = findViewById(R.id.tv_air_pm_value);
        mTipView = findViewById(R.id.line_air_pm);
    }

    public void setValue(String title, String value) {
        if (mTitleTv != null) {
            mTitleTv.setText(title);
        }
        if (mValueTv != null) {
            mValueTv.setText(value);
        }
    }
}
