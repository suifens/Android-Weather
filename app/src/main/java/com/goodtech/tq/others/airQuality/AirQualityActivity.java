package com.goodtech.tq.others.airQuality;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.listener.CompletionListener;
import com.goodtech.tq.models.AirPmModel;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.others.constellation.ConsDetailFragment;
import com.goodtech.tq.others.constellation.ConsOtherFragment;
import com.goodtech.tq.others.constellation.mode.ConsDayMode;
import com.goodtech.tq.others.constellation.mode.ConsMonthMode;
import com.goodtech.tq.others.constellation.mode.ConsWeekMode;
import com.goodtech.tq.others.constellation.mode.ConsYearMode;
import com.goodtech.tq.views.CircleProgressView;
import com.goodtech.tq.views.IArcView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.List;

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


    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
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
                    if (listener != null) listener.onCompletion();
                } catch (Exception e) {
                    if (listener != null) listener.onCompletion();
                    e.printStackTrace();
                }
            }
        });
    }
}