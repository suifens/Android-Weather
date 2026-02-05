package com.goodtech.tq.modules.removeAd.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.goodtech.tq.R
import com.goodtech.tq.databinding.ViewVideoTaskBinding

/**
 * 视频任务自定义View
 * 支持三种状态：选中（selected）、当前（current）、未来（future）
 */
class VideoTaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class TaskState {
        SELECTED,  // 选中状态
        CURRENT,   // 当前状态
        FUTURE     // 未来状态
    }

    private lateinit var binding: ViewVideoTaskBinding
    private var videoNumberText: TextView
    private var rewardText: TextView
    private var hourText: TextView
    private var claimButton: Button
    private var currentState: TaskState = TaskState.CURRENT

    init {
        // 加载布局文件
        binding = ViewVideoTaskBinding.inflate(LayoutInflater.from(context), this, true)
        
        // 获取根视图（LinearLayout）并设置属性
        val rootView = binding.root as? LinearLayout
        rootView?.apply {
            orientation = VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }
        
        // 获取子视图引用
        videoNumberText = binding.videoNumberText
        rewardText = binding.rewardText
        hourText = binding.hourText
        claimButton = binding.claimButton
        
        // 设置初始状态
        setState(TaskState.CURRENT)
    }
    
    /**
     * 设置状态
     */
    fun setState(state: TaskState) {
        currentState = state
        updateState()
    }
    
    /**
     * 获取当前状态
     */
    fun getState(): TaskState = currentState
    
    /**
     * 设置视频编号
     */
    fun setVideoNumber(number: Int) {
        videoNumberText.text = "第${number}个视频"
    }
    
    /**
     * 设置奖励时长
     */
    fun setRewardHours(hours: Int) {
        rewardText.text = "+$hours"
        // 颜色会在 updateState() 中设置
    }
    
    /**
     * 设置按钮文本
     */
    fun setButtonText(text: String) {
        claimButton.text = text
    }
    
    /**
     * 设置按钮点击监听器
     */
    fun setOnClaimClickListener(listener: OnClickListener) {
        claimButton.setOnClickListener(listener)
    }
    
    /**
     * 设置按钮是否可用
     */
    fun setButtonEnabled(enabled: Boolean) {
        claimButton.isEnabled = enabled
    }
    
    /**
     * 更新状态样式
     */
    private fun updateState() {
        val backgroundDrawable: Drawable
        
        when (currentState) {
            TaskState.SELECTED -> {
                // 选中状态：橙色背景，白色文字
                backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_task_completed)!!
                videoNumberText.setTextColor(context.getColor(android.R.color.white))
                rewardText.setTextColor(context.getColor(android.R.color.white))
                hourText.setTextColor(context.getColor(android.R.color.white))
                claimButton.setBackgroundResource(R.drawable.bg_button_claim_s)
                claimButton.setTextColor(context.getColor(R.color.color_orange))
            }
            TaskState.CURRENT -> {
                // 当前状态：浅橙色背景，橙色文字（当前布局的状态）
                backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_task_locked)!!
                videoNumberText.setTextColor(context.getColor(android.R.color.black))
                rewardText.setTextColor(context.getColor(R.color.color_orange))
                hourText.setTextColor(context.getColor(R.color.color_orange))
                claimButton.setBackgroundResource(R.drawable.bg_button_claim)
                claimButton.setTextColor(context.getColor(android.R.color.white))
            }
            TaskState.FUTURE -> {
                // 未来状态：浅橙色背景 + 灰色边框，灰色文字
                backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_task_available)!!
                videoNumberText.setTextColor(context.getColor(R.color.black))
                rewardText.setTextColor(context.getColor(R.color.black))
                hourText.setTextColor(context.getColor(R.color.black))
                claimButton.setBackgroundResource(R.drawable.bg_button_claim_grey)
                claimButton.setTextColor(context.getColor(android.R.color.white))
            }
        }
        
        background = backgroundDrawable
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    // 获取子视图的引用（用于向后兼容）
    fun getVideoNumberTextView(): TextView = videoNumberText
    fun getRewardTextView(): TextView = rewardText
    fun getClaimButton(): Button = claimButton
}
