package com.goodtech.tq.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;

public class ObservationItemView extends LinearLayout {

    public ObservationItemView(Context context) {
        super(context);
        init(context, null);
    }

    public ObservationItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ObservationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private ImageView mIconImgV;
    private TextView mTitleTv;
    private TextView mValueTv;


    /**
     * 初始化参数
     */
    private void init(Context context, AttributeSet attrs) {

        int imgIcon = 0;
        String title = "";

        TypedArray typedArray = null;
        if (attrs != null) {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.ObservationItemView);
            imgIcon = typedArray.getResourceId(R.styleable.ObservationItemView_img_icon, 0);
            title = typedArray.getString(R.styleable.ObservationItemView_tv_title);
            typedArray.recycle();
        }

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View view = LayoutInflater.from(context).inflate(R.layout.linear_item_observation, this, true);
        mIconImgV = view.findViewById(R.id.img_icon);
        if (mIconImgV != null) {
            mIconImgV.setImageResource(imgIcon);
        }

        mTitleTv = view.findViewById(R.id.tv_title);
        if (mTitleTv != null) {
            mTitleTv.setText(title);
        }

        mValueTv = view.findViewById(R.id.tv_value);
    }

    public void setValue(String value) {
        mValueTv.setText(value);
    }

}
