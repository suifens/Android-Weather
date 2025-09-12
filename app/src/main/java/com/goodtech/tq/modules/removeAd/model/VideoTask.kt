package com.goodtech.tq.modules.removeAd.model

/**
 * 视频任务数据模型
 * @param taskNumber 任务编号（1-7）
 * @param rewardHours 奖励时长（小时）
 * @param status 任务状态
 */
data class VideoTask(
    val taskNumber: Int,
    val rewardHours: Int,
    val status: VideoTaskStatus
)

/**
 * 视频任务状态枚举
 */
enum class VideoTaskStatus {
    COMPLETED,  // 已领取：任务已完成，奖励已获得
    AVAILABLE,  // 可领取：任务可执行，可以观看视频获得奖励
    LOCKED      // 未解锁：任务未解锁，需要完成前置任务
}
