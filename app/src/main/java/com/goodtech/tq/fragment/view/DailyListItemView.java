package com.goodtech.tq.fragment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.R;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Daypart;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

@SuppressLint("ViewConstructor")
public class DailyListItemView extends ConstraintLayout {

    public DailyListItemView(Context context) {
        this(context, null);
    }

    public DailyListItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DailyListItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    private DailyItemView mDailyView1;
    private DailyItemView mDailyView2;
    private DailyItemView mDailyView3;
    private DailyItemView mDailyView4;
    private DailyItemView mDailyView5;
    private DailyItemView mDailyView6;
    private DailyItemView mDailyView7;
    private DailyItemView mDailyView8;
    private DailyItemView mDailyView9;
    private DailyItemView mDailyView10;
    
    @SuppressLint("DefaultLocale")
    protected void initData() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.weather_daily_list, this, true);
        mDailyView1 = view.findViewById(R.id.item_daily_1);
        mDailyView2 = view.findViewById(R.id.item_daily_2);
        mDailyView3 = view.findViewById(R.id.item_daily_3);
        mDailyView4 = view.findViewById(R.id.item_daily_4);
        mDailyView5 = view.findViewById(R.id.item_daily_5);
        mDailyView6 = view.findViewById(R.id.item_daily_6);
        mDailyView7 = view.findViewById(R.id.item_daily_7);
        mDailyView8 = view.findViewById(R.id.item_daily_8);
        mDailyView9 = view.findViewById(R.id.item_daily_9);
        mDailyView10 = view.findViewById(R.id.item_daily_10);
    }

    @SuppressLint("DefaultLocale")
    public void setData(WeatherModel model) {
        if (model == null) {
            return;
        }

        for (int i = 0; i < 10; i++) {
            if (model.dailies != null && model.dailies.size() > i) {
                Daily daily = model.dailies.get(i);
                switch (i) {
                    case 0:
                        mDailyView1.setData(model, daily);
                        break;
                    case 1:
                        mDailyView2.setData(model, daily);
                        break;
                    case 2:
                        mDailyView3.setData(model, daily);
                        break;
                    case 3:
                        mDailyView4.setData(model, daily);
                        break;
                    case 4:
                        mDailyView5.setData(model, daily);
                        break;
                    case 5:
                        mDailyView6.setData(model, daily);
                        break;
                    case 6:
                        mDailyView7.setData(model, daily);
                        break;
                    case 7:
                        mDailyView8.setData(model, daily);
                        break;
                    case 8:
                        mDailyView9.setData(model, daily);
                        break;
                    case 9:
                        mDailyView10.setData(model, daily);
                        break;
                }
            }
        }
    }

}
