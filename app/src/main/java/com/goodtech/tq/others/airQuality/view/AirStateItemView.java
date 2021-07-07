package com.goodtech.tq.others.airQuality.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;

public class AirStateItemView extends ConstraintLayout {

    public AirStateItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AirStateItemView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {

        LayoutInflater.from(getContext()).inflate(R.layout.air_stub_state_item, this, true);

        int imgIcon = 0;
        String title = "";
        String state = "";

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AirStateItemView);
        if (typedArray != null) {
            imgIcon = typedArray.getResourceId(R.styleable.AirStateItemView_img_item, 0);
            title = typedArray.getString(R.styleable.AirStateItemView_tv_item_title);
            state = typedArray.getString(R.styleable.AirStateItemView_tv_state);
            typedArray.recycle();
        }

        ImageView imageView = findViewById(R.id.img_item);
        if (imageView != null) {
            imageView.setImageResource(imgIcon);
        }

        TextView mTitleTv = findViewById(R.id.tv_item_title);
        if (mTitleTv != null) {
            mTitleTv.setText(title);
        }

        TextView stateTv = findViewById(R.id.tv_item_state);
        if (stateTv != null) {
            stateTv.setText(state);
        }
    }
}
