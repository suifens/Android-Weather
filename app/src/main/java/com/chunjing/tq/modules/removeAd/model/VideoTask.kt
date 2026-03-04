package com.chunjing.tq.modules.removeAd.model

/**
 * 视频任务数据模型
 */
data class VideoTask(
    val taskNumber: Int,
    val rewardHours: Int,
    val status: VideoTaskStatus
)

enum class VideoTaskStatus {
    COMPLETED,
    AVAILABLE,
    LOCKED
}
