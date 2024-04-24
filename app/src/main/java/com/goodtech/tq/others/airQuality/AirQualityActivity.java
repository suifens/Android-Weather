package com.goodtech.tq.others.airQuality;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.helpers.AqiHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.others.airQuality.view.AirLifeView;
import com.goodtech.tq.views.CircleProgressView;
import com.umeng.analytics.MobclickAgent;

import java.util.Objects;

public class AirQualityActivity extends BaseActivity {

    private static final String EXTRA_CITY = "city";
    private static final String EXTRA_AQI = "aqi";

    public static void redirectTo(Context ctx, CityMode cityMode, int aqi) {
        Intent intent = new Intent(ctx, AirQualityActivity.class);
        intent.putExtra(EXTRA_CITY, cityMode);
        intent.putExtra(EXTRA_AQI, aqi);
        ctx.startActivity(intent);
    }

    private CityMode mCityMode;
    private int mAqi;

//    private IArcView mArcView;
    private ImageView mArcView;
    private CircleProgressView mProgressView;
    private TextView mAqiTv;
    private TextView mAqiTypeTv;
    private TextView mAqiRemindTv;
    private TextView mUpdateTimeTv;
    private AirLifeView mLifeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);

        mCityMode = getIntent().getParcelableExtra(EXTRA_CITY);
        if (mCityMode != null && "1000".equals(mCityMode.getPoiId())) {
            mCityMode = LocationSpHelper.getLocation();
        }
        mAqi = getIntent().getIntExtra(EXTRA_AQI, 0);

        View topBar = findViewById(R.id.layout_top_bar);
        configStationBar(topBar);

        //  返回
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        TextView titleTv = findViewById(R.id.tv_city_name);
        if (titleTv != null) {
            titleTv.setText(mCityMode.getMergerName());
        }

        mArcView = findViewById(R.id.img_top_bg);
        mProgressView = findViewById(R.id.progress_view);
        mAqiTv = findViewById(R.id.tv_air_aqi);
        mAqiTypeTv = findViewById(R.id.tv_aqi_type);
        mAqiRemindTv = findViewById(R.id.tv_aqi_remind);
        mUpdateTimeTv = findViewById(R.id.tv_update_time);
        mLifeView = findViewById(R.id.view_life);

        AirQualityHelper.fetchCityLife(mCityMode, lifeModel -> {
            if (lifeModel != null) {
                mHandler.post(() -> {
                    mLifeView.setVisibility(View.VISIBLE);
                    mLifeView.setupLife(lifeModel, 0);
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        configUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("Ac_AirQuality");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Ac_AirQuality");
        MobclickAgent.onPause(this);
    }

    @SuppressLint("DefaultLocale")
    protected void configUI() {
        mArcView.setImageResource(AqiHelper.getImgResId(mAqi));
        mProgressView.setAngle((float)(Math.min(200, mAqi)/200.0));
        mAqiTv.setText(String.format("%d", mAqi));
        mAqiTypeTv.setText(AqiHelper.getQuality(mAqi));
        mAqiRemindTv.setText(AqiHelper.getRemindString(mAqi));
//        long updateTime = Math.max(AqiHelper.getUpdateTime(), System.currentTimeMillis());
//        mUpdateTimeTv.setText(String.format("更新于今天%s", TimeUtils.longToString(updateTime, "HH:mm")));
    }
}