package com.goodtech.tq.modules.others.constellation;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodtech.tq.R;

/**
 * com.goodtech.tq.others.fortune
 */
public class ScoreView extends LinearLayout {

    private ImageView mStar_1;
    private ImageView mStar_2;
    private ImageView mStar_3;
    private ImageView mStar_4;
    private ImageView mStar_5;

    public ScoreView(Context context) {
        this(context, null);
    }

    public ScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_score, this);

        mStar_1 = findViewById(R.id.star_1);
        mStar_2 = findViewById(R.id.star_2);
        mStar_3 = findViewById(R.id.star_3);
        mStar_4 = findViewById(R.id.star_4);
        mStar_5 = findViewById(R.id.star_5);
    }

    public void setScore(int score) {
        if (score == 0) {
            return;
        }
        switch (score) {
            case 1:
                mStar_1.setImageResource(R.drawable.star_selected);
                break;
            case 2:
                mStar_1.setImageResource(R.drawable.star_selected);
                mStar_2.setImageResource(R.drawable.star_selected);
                break;
            case 3:
                mStar_1.setImageResource(R.drawable.star_selected);
                mStar_2.setImageResource(R.drawable.star_selected);
                mStar_3.setImageResource(R.drawable.star_selected);
                break;
            case 4:
                mStar_1.setImageResource(R.drawable.star_selected);
                mStar_2.setImageResource(R.drawable.star_selected);
                mStar_3.setImageResource(R.drawable.star_selected);
                mStar_4.setImageResource(R.drawable.star_selected);
                break;
            default:
                mStar_1.setImageResource(R.drawable.star_selected);
                mStar_2.setImageResource(R.drawable.star_selected);
                mStar_3.setImageResource(R.drawable.star_selected);
                mStar_4.setImageResource(R.drawable.star_selected);
                mStar_5.setImageResource(R.drawable.star_selected);
                break;
        }
    }

}
