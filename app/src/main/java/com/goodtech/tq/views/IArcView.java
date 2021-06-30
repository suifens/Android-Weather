package com.goodtech.tq.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;

import com.goodtech.tq.R;

@SuppressLint("DrawAllocation")
public class IArcView extends View {

    public IArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IArcView(Context context) {
        super(context);
        init(context, null);
    }

    private int mArcColor;

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IArcView);
        if (typedArray != null) {
            mArcColor = typedArray.getColor(R.styleable.IArcView_arcColor, 0xFFFFFFFF);
            typedArray.recycle();
        }
    }

    public void setColor(@ColorInt int arcColor) {
        mArcColor = arcColor;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(mArcColor);
        paint.setAlpha(128);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        // 画弧形
        canvas.drawCircle(width / 2, height - width * 2, width * 2, paint);

        paint.setAlpha(255);
        // 画弧形
        canvas.drawCircle(width / 2, height - width, width, paint);
        // 画矩形
        RectF top2 = new RectF(0, 0, width, height - width);
        canvas.drawRect(top2, paint);
    }
}