package com.goodtech.tq.others.airQuality.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;

public class AirLifeItemView extends ConstraintLayout {

    public AirLifeItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AirLifeItemView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {

        LayoutInflater.from(getContext()).inflate(R.layout.air_stub_life_item, this, true);

        int imgIcon = 0;
        String title = "";
        String state = "";

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AirLifeItemView);
        if (typedArray != null) {
            imgIcon = typedArray.getResourceId(R.styleable.AirLifeItemView_img_item, 0);
            title = typedArray.getString(R.styleable.AirLifeItemView_tv_item_title);
            state = typedArray.getString(R.styleable.AirLifeItemView_tv_state);
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

        stateTv = findViewById(R.id.tv_item_state);
        if (stateTv != null) {
            stateTv.setText(state);
        }
    }

    private TextView stateTv;

    public void setState(String state) {
        stateTv.setText(state);
    }
}
