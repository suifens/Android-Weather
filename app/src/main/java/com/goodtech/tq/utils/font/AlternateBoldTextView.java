package com.goodtech.tq.utils.font;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class AlternateBoldTextView extends AppCompatTextView {
    public AlternateBoldTextView(Context context) {
        super(context);
    }

    public AlternateBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlternateBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context){
        AssetManager assetManager = context.getAssets();
        Typeface fontType = Typeface.createFromAsset(assetManager,"fonts/DINAlternate-Bold.ttf");
        setTypeface(fontType);
    }
}
