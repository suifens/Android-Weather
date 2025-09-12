package com.goodtech.tq.modules.removeAd.model

/**
 * 每日奖励数据模型
 * @param dayNumber 天数编号（1-7）
 * @param status 奖励状态
 */
data class DailyReward(
    val dayNumber: Int,
    val status: DailyRewardStatus
)

/**
 * 每日奖励状态枚举
 */
enum class DailyRewardStatus {
    COMPLETED,  // 已完成：该天的奖励已经领取
    CURRENT,    // 当前天：当前可以进行签到领取奖励
    LOCKED      // 未解锁：该天还未解锁，需要完成前置天数
}
