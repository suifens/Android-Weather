package com.chunjing.tq.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.Hourly

/**
 */
class HourlyTempChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var topBottom = 0
    private var minTemp = 0
    private var maxTemp = 0
    private var mTemp = 0
    private var mHalfWidth = 0f
    private var mHeight = 0f
    private var mTempPaint: Paint
    private var mTextPaint: Paint
    private var textHeight = 0
    private var tempText = ""
    private var tempTextWidth = 0
    private var usableHeight = 0
    private var tempDiff = 0
    private var density = 0f
    private var pntRadius = 0f

    // 前一天数据
    private var mPrev: Hourly? = null

    // 后一天数据
    private var mNext: Hourly? = null


    init {
        topBottom = SizeUtils.dp2px(8f)
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint.textSize = SizeUtils.sp2px(12f).toFloat()
//        mTextPaint.color = ContextCompat.getColor(context, R.color.color_666)
        mTextPaint.color = Color.parseColor("#00000000")
        textHeight = (mTextPaint.fontMetrics.bottom - mTextPaint.fontMetrics.top).toInt()
        val lineWidth = SizeUtils.dp2px(2f)
        mTempPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTempPaint.strokeWidth = lineWidth.toFloat()
        // 设置线帽，方式折线陡峭时线中间出现裂痕
        mTempPaint.strokeCap = Paint.Cap.SQUARE
        mTempPaint.color = Color.parseColor("#FFD34E")
        pntRadius = SizeUtils.dp2px(4f).toFloat()
    }


    fun setData(minTemp: Int, maxTemp: Int, prev: Hourly?, current: Hourly, next: Hourly?) {
        this.minTemp = minTemp
        this.maxTemp = maxTemp
        mTemp = current.metric.temp
        mPrev = prev
        mNext = next
        tempText = "$mTemp°"
        tempTextWidth = mTextPaint.measureText(tempText).toInt()
        tempDiff = maxTemp - minTemp
        if (usableHeight != 0) {
            density = usableHeight / tempDiff.toFloat()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHalfWidth = measuredWidth / 2f
        mHeight = measuredHeight.toFloat()
        usableHeight = (mHeight - topBottom * 2 - textHeight * 2).toInt()
        density = usableHeight / tempDiff.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(mHalfWidth, 0f)
        val topY = ((maxTemp - mTemp) * density + topBottom + textHeight).toInt()
        canvas.drawCircle(0f, topY.toFloat(), pntRadius, mTempPaint)
        canvas.drawText(
            tempText,
            (-tempTextWidth / 2).toFloat(),
            topY - mTextPaint.fontMetrics.bottom * 2,
            mTextPaint
        )
        // 绘制当前点给前一天数据的连线
        if (mPrev != null) {
            val prev = getEnds(mPrev!!)
            canvas.drawLine(-mHalfWidth,
                (prev.first + topY) / 2f,
                0f,
                topY.toFloat(),
                mTempPaint)
        }
        // 绘制当前点给后一天数据的连线
        if (mNext != null) {
            val next = getEnds(mNext!!)
            canvas.drawLine(0f,
                topY.toFloat(),
                mHalfWidth,
                (next.first + topY) / 2f,
                mTempPaint)
        }
    }

    private fun getEnds(hourly: Hourly): Pair<Int, Int> {
        val topY = ((maxTemp - hourly.metric.temp) * density + topBottom + textHeight).toInt()
        val bottomY = ((maxTemp - hourly.metric.temp) * density + topBottom + textHeight).toInt()
        return Pair(topY, bottomY)
    }
}