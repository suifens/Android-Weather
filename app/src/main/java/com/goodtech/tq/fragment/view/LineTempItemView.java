package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.widget.weatherview.WeatherView;


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class LineTempItemView extends ConstraintLayout {

    public LineTempItemView(Context context) {
        this(context, null);
    }

    public LineTempItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LineTempItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public WeatherView weatherView;
    
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_item_line, this, true);
        weatherView = view.findViewById(R.id.weather_view);

        //画折线
//        weatherView.setLineType(WeatherView.LINE_TYPE_DISCOUNT);
        //画曲线(已修复不圆滑问题)
        weatherView.setLineType(WeatherView.LINE_TYPE_CURVE);

        //设置线宽
        weatherView.setLineWidth(2f);

        //设置一屏幕显示几列(最少3列)
        try {
            weatherView.setColumnNumber(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //设置白天和晚上线条的颜色
        weatherView.setDayAndNightLineColor(Color.parseColor("#FFD34E"), Color.parseColor("#00C4FF"));
    }

    @SuppressLint("DefaultLocale")
    public void setData(final WeatherModel model) {
        if (model != null && model.dailies != null) {
            setVisibility(VISIBLE);
            //填充天气数据
            weatherView.setList(model.dailies);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    weatherView.invalidate();
                }
            }, 100);
        }
    }

}
