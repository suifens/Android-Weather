package com.goodtech.tq.modules.others.airQuality.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.models.LifeEntity;

public class AirLifeView extends ConstraintLayout {

    public AirLifeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AirLifeView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {

        LayoutInflater.from(getContext()).inflate(R.layout.air_stub_life, this, true);

        findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);

        dressingView = findViewById(R.id.item_dressing);
        sportView = findViewById(R.id.item_sport);
        umbrellaView = findViewById(R.id.item_umbrella);
        fishingView = findViewById(R.id.item_fishing);
        coldView = findViewById(R.id.item_cold);
        carView = findViewById(R.id.item_car);
        conditionerView = findViewById(R.id.item_air_conditioner);
        allergyView = findViewById(R.id.item_allergy);
        comfortView = findViewById(R.id.item_comfort);
    }

    private AirLifeItemView dressingView;//穿衣指数
    private AirLifeItemView sportView;  //运动指数
    private AirLifeItemView umbrellaView;//带伞指数
    private AirLifeItemView fishingView;//钓鱼指数
    private AirLifeItemView coldView;   //感冒指数
    private AirLifeItemView carView;    //洗车指数
    private AirLifeItemView conditionerView;//空调开启指数
    private AirLifeItemView allergyView;    //过敏指数
    private AirLifeItemView comfortView;    //舒适度指数

    public void showWhiteType() {
        setBackgroundResource(R.drawable.bg_circle_black_12);
        int color = Color.WHITE;
        dressingView.setTextColor(color);
        sportView.setTextColor(color);
        umbrellaView.setTextColor(color);
        fishingView.setTextColor(color);
        coldView.setTextColor(color);
        carView.setTextColor(color);
        conditionerView.setTextColor(color);
        allergyView.setTextColor(color);
        comfortView.setTextColor(color);
    }

    public void setupLife(LifeEntity lifeModel) {
        if (lifeModel != null) {
            dressingView.setState(lifeModel.getChuanyi() != null ? lifeModel.getChuanyi().getV() : "");
            sportView.setState(lifeModel.getYundong() != null ? lifeModel.getYundong().getV() : "");
            umbrellaView.setState(lifeModel.getDaisan() != null ? lifeModel.getDaisan().getV() : "");
            fishingView.setState(lifeModel.getDiaoyu() != null ? lifeModel.getDiaoyu().getV() : "");
            coldView.setState(lifeModel.getGanmao() != null ? lifeModel.getGanmao().getV() : "");
            carView.setState(lifeModel.getXiche() != null ? lifeModel.getXiche().getV() : "");
            conditionerView.setState(lifeModel.getKongtiao() != null ? lifeModel.getKongtiao().getV() : "");
            allergyView.setState(lifeModel.getGuomin() != null ? lifeModel.getGuomin().getV() : "");
            comfortView.setState(lifeModel.getShushidu() != null ? lifeModel.getShushidu().getV() : "");
        }
    }
}
