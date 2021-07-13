package com.goodtech.tq.others.constellation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.models.constellation.ConsDayMode;
import com.goodtech.tq.models.constellation.ConsMonthMode;
import com.goodtech.tq.models.constellation.ConsWeekMode;
import com.goodtech.tq.models.constellation.ConsYearMode;
import com.goodtech.tq.views.SwitchButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

public class ConstellationDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String EXTRA_CONSTELLATION = "constellation";

    public static void redirectTo(Context ctx, ConstellationEnum constellation) {
        Intent intent = new Intent(ctx, ConstellationDetailActivity.class);
        intent.putExtra(EXTRA_CONSTELLATION, constellation.toString());
        ctx.startActivity(intent);
    }

    private ConstellationEnum mConsEnum;
    private Fragment mFragment;
    private boolean hadLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation_detail);
        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_other).setOnClickListener(this);

        mConsEnum = ConstellationEnum.getConstellationEnum(getIntent().getStringExtra(EXTRA_CONSTELLATION));

        configSegmentTabLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ImageView mIconImgV = findViewById(R.id.image_constellation);
        mIconImgV.setImageResource(mConsEnum.imageRes);
        TextView mNameTv = findViewById(R.id.tv_constellation);
        mNameTv.setText(mConsEnum.name);
        TextView mDateTv = findViewById(R.id.tv_constellation_date);
        mDateTv.setText(mConsEnum.date);

        if (!hadLoad) {
            mHandler.postDelayed(() -> {
                configDay();
                getData("today");

            }, 400);
            hadLoad = true;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.button_other:
                Intent intent = new Intent(this, ConstellationActivity.class);
                this.startActivity(intent);
                break;
        }
    }

    private void configDay() {
        if (mFragment != null && mFragment instanceof ConsDetailFragment) {
            return;
        }
        mFragment = new ConsDetailFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.layout_container, mFragment).commitAllowingStateLoss();
    }

    private void configOther() {
        if (mFragment != null && mFragment instanceof ConsOtherFragment) {
            return;
        }
        mFragment = new ConsOtherFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.layout_container, mFragment).commitAllowingStateLoss();
    }

    /**
     * segment config
     */
    private void configSegmentTabLayout() {
        SwitchButton switchBtn = findViewById(R.id.view_switch);
        String[] titles = new String[] {"今日","明日","本周","本月","今年"};
        switchBtn.setButtonTitles(titles);
        switchBtn.setOnCheckedChangeListener((view, selectedIndex) -> {
            switch (selectedIndex) {
                case 0: {
                    configDay();
                    ConstellationDetailActivity.this.getData("today");
                }
                    break;
                case 1: {
                    configDay();
                    ConstellationDetailActivity.this.getData("tomorrow");
                }
                break;
                case 2: {
                    configOther();
                    ConstellationDetailActivity.this.getData("week");
                }
                    break;
                case 3: {
                    configOther();
                    ConstellationDetailActivity.this.getData("month");
                }
                    break;
                case 4: {
                    configOther();
                    ConstellationDetailActivity.this.getData("year");
                }
                    break;
            }
        });
    }

    private static final String TAG = "ConstellationDetailActi";
    private void getData(String type) {

        if (mFragment instanceof ConsOtherFragment) {
            ((ConsOtherFragment) mFragment).clear();
        }

        JuHeHelper.getInstance().fetchFortune(mConsEnum.name, type, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                Log.e(TAG, "onResponse: " + jsonObject.toString());
                try {
                    if (success) {
                        switch (type) {
                            case "today":
                            case "tomorrow": {
                                ConsDayMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsDayMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsDetailFragment) mFragment).setConsDetailModel(model);
                                }
                            }
                            break;
                            case "week": {
                                ConsWeekMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsWeekMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsOtherFragment) mFragment).setConsWeekModel(model);
                                }
                            }
                            break;
                            case "month": {
                                ConsMonthMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsMonthMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsOtherFragment) mFragment).setConsMonthModel(model);
                                }
                            }
                            break;
                            case "year": {
                                ConsYearMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsYearMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsOtherFragment) mFragment).setConsYearModel(model);
                                }
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}