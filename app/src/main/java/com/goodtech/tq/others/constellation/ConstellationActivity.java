package com.goodtech.tq.others.constellation;

import android.os.Bundle;
import android.view.View;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;

public class ConstellationActivity extends BaseActivity implements View.OnClickListener {
    
    private final ConstellationEnum[] constellations = ConstellationEnum.values();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation);
        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));
        findViewById(R.id.button_back).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.constellation_1:
                
                break;
            case R.id.constellation_2:

                break;
            case R.id.constellation_3:

                break;
            case R.id.constellation_4:

                break;
            case R.id.constellation_5:

                break;
            case R.id.constellation_6:

                break;
            case R.id.constellation_7:

                break;
            case R.id.constellation_8:

                break;
            case R.id.constellation_9:

                break;
            case R.id.constellation_10:

                break;
            case R.id.constellation_11:

                break;
            case R.id.constellation_12:

                break;
        }
    }
}