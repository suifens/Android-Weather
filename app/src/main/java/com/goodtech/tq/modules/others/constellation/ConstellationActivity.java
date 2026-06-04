package com.goodtech.tq.modules.others.constellation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;

@SuppressLint("NonConstantResourceId")
public class ConstellationActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected String getUmPageChannel() {
        return "Ac_Constellation";
    }
    
    private final ConstellationEnum[] constellations = ConstellationEnum.values();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation);
        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.constellation_1).setOnClickListener(this);
        findViewById(R.id.constellation_2).setOnClickListener(this);
        findViewById(R.id.constellation_3).setOnClickListener(this);
        findViewById(R.id.constellation_4).setOnClickListener(this);
        findViewById(R.id.constellation_5).setOnClickListener(this);
        findViewById(R.id.constellation_6).setOnClickListener(this);
        findViewById(R.id.constellation_7).setOnClickListener(this);
        findViewById(R.id.constellation_8).setOnClickListener(this);
        findViewById(R.id.constellation_9).setOnClickListener(this);
        findViewById(R.id.constellation_10).setOnClickListener(this);
        findViewById(R.id.constellation_11).setOnClickListener(this);
        findViewById(R.id.constellation_12).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            finish();
        } else if (v.getId() == R.id.constellation_1) {
            ConstellationDetailActivity.redirectTo(this, constellations[0]);
        } else if (v.getId() == R.id.constellation_2) {
            ConstellationDetailActivity.redirectTo(this, constellations[1]);
        } else if (v.getId() == R.id.constellation_3) {
            ConstellationDetailActivity.redirectTo(this, constellations[2]);
        } else if (v.getId() == R.id.constellation_4) {
            ConstellationDetailActivity.redirectTo(this, constellations[3]);
        } else if (v.getId() == R.id.constellation_5) {
            ConstellationDetailActivity.redirectTo(this, constellations[4]);
        } else if (v.getId() == R.id.constellation_6) {
            ConstellationDetailActivity.redirectTo(this, constellations[5]);
        } else if (v.getId() == R.id.constellation_7) {
            ConstellationDetailActivity.redirectTo(this, constellations[6]);
        } else if (v.getId() == R.id.constellation_8) {
            ConstellationDetailActivity.redirectTo(this, constellations[7]);
        } else if (v.getId() == R.id.constellation_9) {
            ConstellationDetailActivity.redirectTo(this, constellations[8]);
        } else if (v.getId() == R.id.constellation_10) {
            ConstellationDetailActivity.redirectTo(this, constellations[9]);
        } else if (v.getId() == R.id.constellation_11) {
            ConstellationDetailActivity.redirectTo(this, constellations[10]);
        } else if (v.getId() == R.id.constellation_12) {
            ConstellationDetailActivity.redirectTo(this, constellations[11]);
        }
    }
}