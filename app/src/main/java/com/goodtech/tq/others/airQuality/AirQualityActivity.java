package com.goodtech.tq.others.airQuality;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.listener.CompletionListener;
import com.goodtech.tq.models.AirPmModel;
import com.goodtech.tq.models.AirQualityModel;
import com.goodtech.tq.others.airQuality.view.AirPmView;
import com.goodtech.tq.views.CircleProgressView;
import com.goodtech.tq.views.IArcView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

public class AirQualityActivity extends BaseActivity {

    private static final String TAG = "AirQualityActivity";
    private static final String EXTRA_CITY = "city";

    public static void redirectTo(Context ctx, String city) {
        Intent intent = new Intent(ctx, AirQualityActivity.class);
        intent.putExtra(EXTRA_CITY, city);
        ctx.startActivity(intent);
    }

    private String mCity;
    private AirPmModel mPmModel;

    private IArcView mArcView;
    private CircleProgressView mProgressView;
    private TextView mAqiTv;
    private TextView mAqiTypeTv;
    private TextView mAqiRemindTv;
    private TextView mUpdateTimeTv;
    private AirPmView mAirPmView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);

        mCity = getIntent().getStringExtra(EXTRA_CITY);

        View topBar = findViewById(R.id.layout_top_bar);
        configStationBar(topBar);

        //  返回
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        TextView titleTv = findViewById(R.id.tv_city_name);
        if (titleTv != null) {
            titleTv.setText(mCity);
        }

        mArcView = findViewById(R.id.img_top_bg);
        mProgressView = findViewById(R.id.progress_view);
        mAqiTv = findViewById(R.id.tv_air_aqi);
        mAqiTypeTv = findViewById(R.id.tv_aqi_type);
        mAqiRemindTv = findViewById(R.id.tv_aqi_remind);
        mUpdateTimeTv = findViewById(R.id.tv_update_time);
        mAirPmView = findViewById(R.id.layout_air_pm);

        mPmModel = new AirPmModel();
        mPmModel.setPm25("10");
        mPmModel.setAqi("90");
        mPmModel.setPm10("30");
        mPmModel.setCo("0.8");
        mPmModel.setNo2("32");
        mPmModel.setOzone("23");
        mPmModel.setSo2("41");
        mPmModel.setQuality("优");
        mPmModel.setTime("2021-4-29 12:32:28");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
        configUI();
    }

    protected void configUI() {
        mArcView.setColor(mPmModel.getColorResId());
        mProgressView.setAngle((float) Math.min(200, Float.parseFloat(mPmModel.getPm25()) / 200));
        mAqiTv.setText(mPmModel.getAqi());
        mAqiTypeTv.setText(mPmModel.getQuality());
        mAqiRemindTv.setText(mPmModel.getRemindString());
        mAirPmView.setValue(mPmModel);
    }

    private void initData() {
        if (!TextUtils.isEmpty(mCity)) {
            getData(mCity, () -> {

            });
        }
    }

    private void getData(String city, CompletionListener listener) {
        JuHeHelper.getInstance().fetchAirPM(city, new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                Log.e(TAG, "onResponse: " + jsonObject.toString());
                try {
                    if (success) {
                        JsonObject resultJson = new Gson().fromJson(jsonObject.optString("result"), JsonObject.class);
                        if (resultJson != null) {
                            mPmModel = new Gson().fromJson(resultJson, new TypeToken<AirPmModel>() {
                            }.getType());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listener != null) listener.onCompletion();
            }
        });
    }
}