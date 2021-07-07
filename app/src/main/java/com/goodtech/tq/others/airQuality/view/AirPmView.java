package com.goodtech.tq.others.airQuality.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.models.AirPmModel;

public class AirPmView extends ConstraintLayout {

    public AirPmView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AirPmView(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.air_stub_pm, this, true);
    }

    public void setValue(AirPmModel pmModel) {
        AirPmItemView pm2 = findViewById(R.id.view_pm2);
        if (pm2 != null) {
            pm2.setValue("PM2.5", pmModel.getPm25());
        }
        AirPmItemView pm10 = findViewById(R.id.view_pm10);
        if (pm10 != null) {
            pm10.setValue("PM10", pmModel.getPm25());
        }
        AirPmItemView so2 = findViewById(R.id.view_so2);
        if (so2 != null) {
            so2.setValue("SO2", pmModel.getSo2());
        }
        AirPmItemView no2 = findViewById(R.id.view_no2);
        if (no2 != null) {
            no2.setValue("NO2", pmModel.getNo2());
        }
        AirPmItemView ozone = findViewById(R.id.view_o3);
        if (ozone != null) {
            ozone.setValue("O3", pmModel.getOzone());
        }
        AirPmItemView co = findViewById(R.id.view_co);
        if (co != null) {
            co.setValue("CO", pmModel.getCo());
        }
    }
}
