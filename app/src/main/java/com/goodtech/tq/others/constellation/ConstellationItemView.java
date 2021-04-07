package com.goodtech.tq.others.constellation;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;

/**
 * com.goodtech.tq.others.fortune
 */
public class ConstellationItemView extends LinearLayout {

    public ConstellationItemView(Context context) {
        this(context, null);
    }

    public ConstellationItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_constellation_item, this);

        ImageView mIconImgV = findViewById(R.id.image_constellation);
        TextView mNameTv = findViewById(R.id.tv_constellation);
        TextView mDateTv = findViewById(R.id.tv_constellation_date);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConstellationItemView);
        if (typedArray != null) {

            int imageRes = typedArray.getResourceId(R.styleable.ConstellationItemView_icon, 0);
            if (imageRes != 0) {
                mIconImgV.setImageResource(imageRes);
            }

            String name = typedArray.getString(R.styleable.ConstellationItemView_constellation);
            if (!TextUtils.isEmpty(name)) {
                mNameTv.setText(name);
            }

            String date = typedArray.getString(R.styleable.ConstellationItemView_date);
            if (!TextUtils.isEmpty(date)) {
                mDateTv.setText(date);
            }
            typedArray.recycle();
        }
    }

}
