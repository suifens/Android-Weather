package com.chunjing.tq.modules.removeAd.model

/**
 * 每日奖励数据模型
 */
data class DailyReward(
    val dayNumber: Int,
    val status: DailyRewardStatus
)

enum class DailyRewardStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}
