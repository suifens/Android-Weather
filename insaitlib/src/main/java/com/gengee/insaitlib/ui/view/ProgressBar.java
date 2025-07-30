package com.gengee.insaitlib.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;


public class ProgressBar extends View {
    /**
     * 分段颜色
     */
    private int[] sectionColors;
    
    private int bgColor;
    private int bgStrokeColor;
    /**
     * 进度条最大值
     */
    private float maxValue;
    /**
     * 进度条当前值
     */
    private float process;

    private float lowProportion = 0.2f;
    /**
     * 画笔
     */
    private Paint mPaint;
    
    private int mWidth, mHeight;
    
    //背景为描边，非填充
    private boolean isStrokeBg;
    
    //进度是否从左边开始
    private boolean isStartLeft;
    
    public ProgressBar(Context context, AttributeSet attrs,
                       int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    
    public ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    
    public ProgressBar(Context context) {
        super(context);
        initView(context);
    }
    
    protected int oneDipPx;
    
    private void initView(Context context) {
    
        bgColor = Color.parseColor("#000000");
        bgStrokeColor = Color.parseColor("#000000");
        sectionColors = new int[]{Color.parseColor("#000000"),
                Color.parseColor("#FFF83F4F")};
    
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        oneDipPx = SizeUtils.dp2px(1);
        mPaint.setStrokeWidth(oneDipPx);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        //draw background
        int round = 0;
        mPaint.setColor(bgColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);// 设置画笔的锯齿效果
       
        
        if (maxValue > 0) {
            float section = process / maxValue;
            float processWidth = mWidth * section;
            RectF rectProgressBg = null;
            rectProgressBg = new RectF(0, 0, processWidth, mHeight);
            if (section > lowProportion) {
                mPaint.setColor(sectionColors[0]);
            } else if (section != 0.0f) {
                mPaint.setColor(sectionColors[1]);
            } else {
                mPaint.setColor(Color.TRANSPARENT);
            }
//            LinearGradient shader = new LinearGradient(3, 3, (mWidth - 3) * section, mHeight - 3, colors, null, Shader.TileMode.MIRROR);
//            mPaint.setShader(shader);
//        }
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rectProgressBg, round, round, mPaint);
        }
        
        
    }
    
    public void setStrokeBg(boolean strokeBg) {
        isStrokeBg = strokeBg;
    }
    
    public void setStartLeft(boolean startLeft) {
        isStartLeft = startLeft;
    }
    
    public void setSectionColors(int[] sectionColors) {
        this.sectionColors = sectionColors;
    }
    
    /***
     * 设置最大的进度值
     * @param max
     */
    public void setMax(float max) {
        this.maxValue = max;
    }

    public void setLowProportion(float proportion) {
        this.lowProportion = proportion;
    }
    
    /***
     * 设置当前的进度值
     * @param process
     */
    public void setProcess(float process) {
        this.process = process > maxValue ? maxValue : process;
        invalidate();
    }
    
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        invalidate();
    }
    
    
    public void setBgStrokeColor(int bgStrokeColor) {
        this.bgStrokeColor = bgStrokeColor;
        invalidate();
    }
    
    public float getMax() {
        return maxValue;
    }
    
    public float getProcess() {
        return process;
    }
    
    private int dipToPx(int dip) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.EXACTLY || widthSpecMode == MeasureSpec.AT_MOST) {
            mWidth = widthSpecSize;
        } else {
            mWidth = 0;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            mHeight = dipToPx(10);
        } else {
            mHeight = heightSpecSize;
        }
        setMeasuredDimension(mWidth, mHeight);
    }
    
}
