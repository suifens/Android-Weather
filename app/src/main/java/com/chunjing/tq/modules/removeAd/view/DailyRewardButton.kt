package com.chunjing.tq.modules.removeAd.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R

class DailyRewardButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var isSelectedState = false
    private var checkmarkDrawable: Drawable? = null
    private val defaultPaddingLeft = paddingLeft
    private val defaultPaddingTop = paddingTop
    private val defaultPaddingRight = paddingRight
    private val defaultPaddingBottom = paddingBottom

    init {
        gravity = Gravity.CENTER
        textSize = 14f
        minHeight = SizeUtils.dp2px(40f)
        minWidth = 0
        checkmarkDrawable = ContextCompat.getDrawable(context, R.drawable.ic_seclected_org)
        setSelectedState(false)
    }

    fun setSelectedState(selected: Boolean) {
        isSelectedState = selected
        updateState()
    }

    fun setDayText(day: Int) { text = "第${day}天" }

    private fun updateState() {
        if (isSelectedState) {
            setBackgroundResource(R.drawable.bg_daily_reward_selected)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            checkmarkDrawable?.let {
                val iconSize = SizeUtils.dp2px(20f)
                it.setBounds(0, 0, iconSize, iconSize)
                setCompoundDrawables(null, null, it, null)
                setPadding(defaultPaddingLeft, defaultPaddingTop, SizeUtils.dp2px(6f), defaultPaddingBottom)
            } ?: setCompoundDrawables(null, null, null, null)
        } else {
            setBackgroundResource(R.drawable.bg_daily_reward_unselected)
            setTextColor(ContextCompat.getColor(context, R.color.color_42a0fb))
            setCompoundDrawables(null, null, null, null)
            setPadding(defaultPaddingLeft, defaultPaddingTop, defaultPaddingRight, defaultPaddingBottom)
        }
    }
}
