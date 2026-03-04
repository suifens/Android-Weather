package com.chunjing.tq.modules.removeAd.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chunjing.tq.R
import com.chunjing.tq.databinding.ViewVideoTaskBinding

class VideoTaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class TaskState { SELECTED, CURRENT, FUTURE }

    private val binding = ViewVideoTaskBinding.inflate(LayoutInflater.from(context), this, true)
    private val videoNumberText: TextView = binding.videoNumberText
    private val rewardText: TextView = binding.rewardText
    private val hourText: TextView = binding.hourText
    private val claimButton: Button = binding.claimButton
    private var currentState: TaskState = TaskState.CURRENT

    init {
        (binding.root as? LinearLayout)?.apply {
            orientation = VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }
        setState(TaskState.CURRENT)
    }

    fun setState(state: TaskState) {
        currentState = state
        updateState()
    }

    fun setVideoNumber(number: Int) { videoNumberText.text = "第${number}个视频" }
    fun setRewardHours(hours: Int) { rewardText.text = "+$hours" }
    fun setButtonText(text: String) { claimButton.text = text }
    fun setOnClaimClickListener(listener: OnClickListener) { claimButton.setOnClickListener(listener) }
    fun setButtonEnabled(enabled: Boolean) { claimButton.isEnabled = enabled }

    private fun updateState() {
        val bg: Drawable = when (currentState) {
            TaskState.SELECTED -> ContextCompat.getDrawable(context, R.drawable.bg_video_task_completed)!!
            TaskState.CURRENT -> ContextCompat.getDrawable(context, R.drawable.bg_video_task_locked)!!
            TaskState.FUTURE -> ContextCompat.getDrawable(context, R.drawable.bg_video_task_available)!!
        }
        background = bg
        when (currentState) {
            TaskState.SELECTED -> {
                videoNumberText.setTextColor(context.getColor(android.R.color.white))
                rewardText.setTextColor(context.getColor(android.R.color.white))
                hourText.setTextColor(context.getColor(android.R.color.white))
                claimButton.setBackgroundResource(R.drawable.bg_button_claim_s)
                claimButton.setTextColor(context.getColor(R.color.color_orange))
            }
            TaskState.CURRENT -> {
                videoNumberText.setTextColor(context.getColor(android.R.color.black))
                rewardText.setTextColor(context.getColor(R.color.color_orange))
                hourText.setTextColor(context.getColor(R.color.color_orange))
                claimButton.setBackgroundResource(R.drawable.bg_button_claim)
                claimButton.setTextColor(context.getColor(android.R.color.white))
            }
            TaskState.FUTURE -> {
                videoNumberText.setTextColor(context.getColor(android.R.color.black))
                rewardText.setTextColor(context.getColor(android.R.color.black))
                hourText.setTextColor(context.getColor(android.R.color.black))
                claimButton.setBackgroundResource(R.drawable.bg_button_claim_grey)
                claimButton.setTextColor(context.getColor(android.R.color.white))
            }
        }
    }
}
