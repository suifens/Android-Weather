package com.goodtech.tq.base.share;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.goodtech.tq.R;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.utils.DeviceUtils;

public class ShareFootView extends LinearLayout {

    public ShareFootView(Context context) {
        super(context);
        initView();
    }

    public ShareFootView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ShareFootView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View view = LayoutInflater.from(getContext()).inflate(R.layout.share_recycler_bottom, this, true);

        TextView textView = view.findViewById(R.id.tv_app_name);
        textView.setText(DeviceUtils.getAppName(BaseApp.getInstance()));
    }
}
