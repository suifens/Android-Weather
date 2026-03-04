package com.chunjing.tq.utils.font;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

public class AlternateBoldTextView extends AppCompatTextView {
    private static final String TAG = "AlternateBoldTextView";
    private static final String FONT_PATH = "fonts/DINAlternate-Bold.ttf";
    private static Typeface sTypeface;

    public AlternateBoldTextView(Context context) {
        super(context);
        init(context);
    }

    public AlternateBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlternateBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (sTypeface == null) {
            try {
                AssetManager assetManager = context.getAssets();
                sTypeface = Typeface.createFromAsset(assetManager, FONT_PATH);
            } catch (Exception e) {
                Log.e(TAG, "Error loading font: " + e.getMessage());
                // 如果加载失败，使用默认字体
                sTypeface = Typeface.DEFAULT;
            }
        }
        setTypeface(sTypeface);
    }
}
