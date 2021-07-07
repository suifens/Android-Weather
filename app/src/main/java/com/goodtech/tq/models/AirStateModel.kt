package com.goodtech.tq.models

import androidx.lifecycle.ViewModel
import com.goodtech.tq.R

/**
 * com.goodtech.tq.models
 */
class AirStateModel : ViewModel() {

    var index = 0
        private set

    val imageRes:Int
        get() {
            return when (index) {
                2 -> R.drawable.ic_tip2
                3 -> R.drawable.ic_tip3
                4 -> R.drawable.ic_tip4
                else -> R.drawable.ic_tip1
            }
        }

    val title:String
        get() {
            return when (index) {
                2 -> "尽量减少室内花粉量"
                3 -> "查询天气情况"
                4 -> "了解适合您的过敏缓解方法"
                else -> "户外活动后及时洗澡"
            }
        }

    val state:String
        get() {
            return when (index) {
                2 -> "请关闭窗户，并使用空调或 HEPA 净化器过滤过敏原。"
                3 -> "了解风况等天气条件何时会增加花粉含量，以便做好准备。"
                4 -> "从服用药物到鼻腔冲洗，与您的医生讨论适合您的选择。"
                else -> "要清除您在室外沾到的花粉，请洗澡并更换衣物。"
            }
        }

}