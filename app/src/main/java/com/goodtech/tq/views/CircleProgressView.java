package com.goodtech.tq.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.goodtech.tq.utils.Utils;

/**
 * com.goodtech.tq.views
 */
@SuppressLint("DrawAllocation")
public class CircleProgressView extends View {

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleProgressView(Context context) {
        super(context);
    }

    private float mValue;
    //  angle : 0 - 360
    public void setAngle(float angle) {
        mValue = angle / 360;
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
        float paging = Utils.dp2px(8);
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(0xFFFFFFFF);
        paint.setAlpha(50);
        paint.setStrokeWidth(paging * 2);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        RectF rectF = new RectF(paging, paging, width - paging, height - paging);
        canvas.drawArc(rectF, 0, 360, false, paint);

        if (mValue > 0) {
            paint.setAlpha(255);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawArc(rectF, 135, Math.min(mValue, 270), false, paint);
        }
    }
}
