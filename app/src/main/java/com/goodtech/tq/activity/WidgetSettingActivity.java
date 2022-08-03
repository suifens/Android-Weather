package com.goodtech.tq.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.goodtech.tq.R;
import com.goodtech.tq.others.widget.WidgetType;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.widget.MyWidget;
import com.umeng.analytics.MobclickAgent;

public class WidgetSettingActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private String tipString = "1.长按“桌面空白处”添加天气小插件，调节不同样式\n" +
            "2.选择“小部件”或“添加插件”\n" +
            "3.找到“天气预报”小插件，长按拖动至桌面\n" +
            "\n" +
            "*oppo手机需要在桌面“两指捏合”添加小工具/小部件\n" +
            "*为保证插件及通知功能的正常使用，请在系统中设置\n" +
            "为允许自动启动(开机启动或后台运行)";

    private ToggleButton clearBtn;
    private Button singleBtn;
    private Button doubleBtn;
    private ImageView previewImgView;

    private Boolean isSingle = true;
    private Boolean isClear = false;

    private Boolean hadChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_widget);

        TextView mTitleView = findViewById(R.id.tv_title);
        if (mTitleView != null) mTitleView.setText(R.string.title_setting_widget);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        singleBtn = findViewById(R.id.singleBtn);
        singleBtn.setOnClickListener(v -> {
            selectedSingle();
            hadChanged = true;
        });

        doubleBtn = findViewById(R.id.doubleBtn);
        doubleBtn.setOnClickListener(v -> {
            selectedDouble();
            hadChanged = true;
        });

        clearBtn = findViewById(R.id.switchBtn_clear);
        clearBtn.setOnClickListener(v -> {
            isClear = clearBtn.isChecked();
            changePreview();
            hadChanged = true;
        });

        previewImgView = findViewById(R.id.previewImgView);

        configSpannable();

        String widgetTypeStr = SpUtils.getInstance().getString(Constants.WIDGET_TYPE, WidgetType.SingleLine1.toString());
        configType(WidgetType.valueOf(widgetTypeStr));

    }

    @Override
    protected void onDestroy() {
        if (hadChanged) {
//            this.startService(new Intent(this, WidgetService.class));
            Intent intent = new Intent(this, MyWidget.class);
            intent.putExtra("WidgetUpdate", true);
            sendBroadcast(intent);
        }
        super.onDestroy();
    }

    private void selectedSingle() {
        setup(singleBtn, true);
        setup(doubleBtn, false);
        isSingle = true;
        changePreview();
    }

    private void selectedDouble() {
        setup(doubleBtn, true);
        setup(singleBtn, false);
        isSingle = false;
        changePreview();
    }

    private void setup(Button btn, Boolean isSelected) {
        if (isSelected) {
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setTextColor(Color.WHITE);

        } else {
            btn.setBackgroundColor(Color.WHITE);
            btn.setTextColor(Color.parseColor("#5A9EF2"));
        }
    }

    private void changePreview() {
        WidgetType type;
        if (isSingle) {
            type = isClear ? WidgetType.SingleLine2 : WidgetType.SingleLine1;
        } else {
            type = isClear ? WidgetType.DoubleLine2 : WidgetType.DoubleLine1;
        }
        changePreview(type);
    }

    private void changePreview(WidgetType type) {
        SpUtils.getInstance().putString(Constants.WIDGET_TYPE, type.toString());
        switch (type) {
            case SingleLine1:
                previewImgView.setImageResource(R.drawable.img_preview_1);
                break;
            case SingleLine2:
                previewImgView.setImageResource(R.drawable.img_preview_2);
                break;
            case DoubleLine1:
                previewImgView.setImageResource(R.drawable.img_preview_3);
                break;
            case DoubleLine2:
                previewImgView.setImageResource(R.drawable.img_preview_4);
                break;
        }
    }

    private void configType(WidgetType type) {
        switch (type) {
            case SingleLine1:
                selectedSingle();
                clearBtn.setChecked(false);
                break;
            case SingleLine2:
                selectedSingle();
                clearBtn.setChecked(true);
                break;
            case DoubleLine1:
                selectedDouble();
                clearBtn.setChecked(false);
                break;
            case DoubleLine2:
                selectedDouble();
                clearBtn.setChecked(true);
                break;
        }
        changePreview(type);
    }

    private void configSpannable() {
        SpannableString spannableString = new SpannableString(tipString);
        String blueString = "“桌面空白处”";
        int agreementStart = tipString.indexOf(blueString);
        int agreementEnd = agreementStart + blueString.length();
        ForegroundColorSpan agreementColorSp = new ForegroundColorSpan(Color.parseColor("#5A9EF2"));
        spannableString.setSpan(agreementColorSp, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        TextView tipTv = findViewById(R.id.tipTextView);
        tipTv.setText(spannableString);
    }

    
}
