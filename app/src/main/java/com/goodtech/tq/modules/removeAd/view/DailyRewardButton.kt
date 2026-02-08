package com.goodtech.tq.modules.removeAd.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.goodtech.tq.R

/**
 * 每日奖励按钮自定义View
 * 支持选中和未选中两种状态
 */
class DailyRewardButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {

    private var isSelectedState: Boolean = false
    private var checkmarkDrawable: Drawable? = null
    private var defaultPaddingLeft: Int = 0
    private var defaultPaddingTop: Int = 0
    private var defaultPaddingRight: Int = 0
    private var defaultPaddingBottom: Int = 0

    init {
        // 保存默认 padding
        defaultPaddingLeft = paddingLeft
        defaultPaddingTop = paddingTop
        defaultPaddingRight = paddingRight
        defaultPaddingBottom = paddingBottom
        
        // 设置按钮属性
        gravity = Gravity.CENTER
        textSize = 14f
        minHeight = SizeUtils.dp2px(40f)
        minWidth = 0

        // 加载对勾图标（优先使用 vector drawable，如果不存在则尝试 PNG）
        checkmarkDrawable = ContextCompat.getDrawable(context, R.drawable.ic_seclected_org)

        // 设置初始状态为未选中
        setSelectedState(false)
    }

    /**
     * 设置选中状态
     * @param selected true 为选中状态，false 为未选中状态
     */
    fun setSelectedState(selected: Boolean) {
        isSelectedState = selected
        updateState()
    }

    /**
     * 获取当前是否选中
     */
    fun isSelectedState(): Boolean = isSelectedState

    /**
     * 设置天数文本
     */
    fun setDayText(day: Int) {
        text = "第${day}天"
    }

    /**
     * 更新状态样式
     */
    private fun updateState() {
        if (isSelectedState) {
            // 选中状态：蓝色背景，白色文字，浅蓝色边框，右侧有橙色对勾图标
            setBackgroundResource(R.drawable.bg_daily_reward_selected)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))

            // 设置右侧图标（对勾图标）
            checkmarkDrawable?.let { drawable ->
                val iconSize = SizeUtils.dp2px(20f)
                drawable.setBounds(0, 0, iconSize, iconSize)
                // 设置右侧图标，左侧、顶部、底部为null
                setCompoundDrawables(null, null, drawable, null)
                // 设置图标距离右边的距离为 10dp
                val paddingEnd = SizeUtils.dp2px(6f)
                setPadding(defaultPaddingLeft, defaultPaddingTop, paddingEnd, defaultPaddingBottom)
            } ?: run {
                // 如果没有图标，移除所有图标
                setCompoundDrawables(null, null, null, null)
                // 恢复默认 padding
                setPadding(defaultPaddingLeft, defaultPaddingTop, defaultPaddingRight, defaultPaddingBottom)
            }
        } else {
            // 未选中状态：白色背景，蓝色文字，浅蓝色边框，没有图标
            setBackgroundResource(R.drawable.bg_daily_reward_unselected)
            setTextColor(ContextCompat.getColor(context, R.color.color_theme))

            // 移除图标
            setCompoundDrawables(null, null, null, null)
            // 恢复默认 padding
            setPadding(defaultPaddingLeft, defaultPaddingTop, defaultPaddingRight, defaultPaddingBottom)
        }
    }
}
