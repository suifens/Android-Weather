package com.goodtech.tq.modules.others.constellation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class ConstellationDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String EXTRA_CONSTELLATION = "constellation";

    public static void redirectTo(Context ctx, ConstellationEnum constellation) {
        Intent intent = new Intent(ctx, ConstellationDetailActivity.class);
        intent.putExtra(EXTRA_CONSTELLATION, constellation.toString());
        ctx.startActivity(intent);
    }

    private ConstellationEnum mConsEnum;
    private boolean hadLoad;
    private ViewPager2 viewPager;
    private SwitchButton mSwitchView;
    private List<Fragment> mList;
    private boolean isSwitchClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation_detail);
        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_other).setOnClickListener(this);

        configViewPager();

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
                getData("today");
            }, 400);
            hadLoad = true;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            finish();
        } else if (v.getId() == R.id.button_other) {
            finishToRight();
        }
    }

    private void configViewPager() {
        mList = new ArrayList<>();
        mList.add(new ConsDetailFragment());
        mList.add(new ConsDetailFragment());
        mList.add(new ConsOtherFragment());
        mList.add(new ConsOtherFragment());
        mList.add(new ConsOtherFragment());

        viewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, mList);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (isSwitchClick) {
                    isSwitchClick = false;
                } else {
                    mSwitchView.setSelectIndex(position);
                    getData(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    /**
     * segment config
     */
    private void configSegmentTabLayout() {
        mSwitchView = findViewById(R.id.view_switch);
        String[] titles = new String[]{"今日", "明日", "本周", "本月", "今年"};
        mSwitchView.setButtonTitles(titles);
        mSwitchView.setOnCheckedChangeListener((view, selectedIndex) -> {
            isSwitchClick = true;
            viewPager.setCurrentItem(selectedIndex, true);
            getData(selectedIndex);
        });
    }

    private void getData(int selectedIndex) {
        switch (selectedIndex) {
            case 0:
                if (((ConsDetailFragment) mList.get(0)).getConsModel() == null) {
                    ConstellationDetailActivity.this.getData("today");
                }
                break;
            case 1:
                if (((ConsDetailFragment) mList.get(1)).getConsModel() == null) {
                    ConstellationDetailActivity.this.getData("tomorrow");
                }

                break;
            case 2:
                if (((ConsOtherFragment) mList.get(2)).getWeekModel() == null) {
                    ConstellationDetailActivity.this.getData("week");
                }
                break;
            case 3:
                if (((ConsOtherFragment) mList.get(3)).getMonthMode() == null) {
                    ConstellationDetailActivity.this.getData("month");
                }
                break;
            case 4:
                if (((ConsOtherFragment) mList.get(4)).getYearMode() == null) {
                    ConstellationDetailActivity.this.getData("year");
                }
                break;
        }
    }

    private static final String TAG = "ConstellationDetailActi";

    private void getData(String type) {

        JuHeHelper.getInstance().fetchFortune(mConsEnum.name, type, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (jsonObject != null && success) {
                        switch (type) {
                            case "today": {
                                ConsDayMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsDayMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsDetailFragment) mList.get(0)).setConsDetailModel(model);
                                }
                            }
                            break;
                            case "tomorrow": {
                                ConsDayMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsDayMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsDetailFragment) mList.get(1)).setConsDetailModel(model);
                                }
                            }
                            break;
                            case "week": {
                                ConsWeekMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsWeekMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsOtherFragment) mList.get(2)).setConsWeekModel(model);
                                }
                            }
                            break;
                            case "month": {
                                ConsMonthMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsMonthMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsOtherFragment) mList.get(3)).setConsMonthModel(model);
                                }
                            }
                            break;
                            case "year": {
                                ConsYearMode model = new Gson().fromJson(String.valueOf(jsonObject), new TypeToken<ConsYearMode>() {
                                }.getType());
                                if (model != null) {
                                    ((ConsOtherFragment) mList.get(4)).setConsYearModel(model);
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