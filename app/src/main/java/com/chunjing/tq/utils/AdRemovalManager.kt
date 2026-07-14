package com.chunjing.tq.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.goodtech.weatherlib.utils.SpUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 去广告时间管理工具类
 * 负责保存和检查去广告状态
 */
object AdRemovalManager {
    private const val TAG = "AdRemovalManager"

    // SharedPreferences key
    private const val KEY_AD_REMOVAL_END_TIME = "ad_removal_end_time"
    private const val KEY_VIDEO_TASK_COMPLETED = "video_task_completed_"
    private const val KEY_VIDEO_TASKS_RESET_DATE = "video_tasks_reset_date"
    private const val KEY_CONTINUOUS_DAYS = "continuous_video_days"
    private const val KEY_LAST_VIDEO_DATE = "last_video_watch_date"
    private const val KEY_DAILY_REWARD_CLAIMED = "daily_reward_claimed_"

    private val sp get() = SpUtils.instance

    private val _adRemovalChanged = MutableLiveData<Long>()
    /** 去广告状态变更（增加时长 / 清除），供首页隐藏广告 */
    val adRemovalChanged: LiveData<Long> = _adRemovalChanged

    fun notifyAdRemovalChanged() {
        _adRemovalChanged.postValue(System.currentTimeMillis())
    }
    /**
     * 添加去广告时长
     */
    fun addAdRemovalTime(hours: Int = 24) {
        val currentTime = System.currentTimeMillis()
        val existingEndTime = sp.getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        val baseTime = if (existingEndTime > currentTime) existingEndTime else currentTime
        val endTime = baseTime + (hours * 60 * 60 * 1000L)
        sp.putLong(KEY_AD_REMOVAL_END_TIME, endTime)
        Log.d(TAG, "添加去广告时长: ${hours}小时，结束时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(endTime))}")
        notifyAdRemovalChanged()
    }

    /**
     * 检查是否在去广告有效期内
     */
    fun isAdRemovalActive(): Boolean {
        val endTime = sp.getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        if (endTime == 0L) return false
        val isActive = System.currentTimeMillis() < endTime
        if (!isActive && endTime > 0) {
            sp.remove(KEY_AD_REMOVAL_END_TIME)
            Log.d(TAG, "去广告时间已过期，已清除")
        }
        return isActive
    }

    fun getRemainingTimeMillis(): Long {
        val endTime = sp.getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        if (endTime == 0L) return 0L
        return (endTime - System.currentTimeMillis()).coerceAtLeast(0)
    }

    fun getRemainingTime(): Pair<Int, Int> {
        val (days, hours, _) = getRemainingTimeDetail()
        return Pair(days, hours)
    }

    fun getRemainingTimeDetail(): Triple<Int, Int, Int> {
        val remainingMillis = getRemainingTimeMillis()
        if (remainingMillis <= 0) return Triple(0, 0, 0)
        val totalMinutes = (remainingMillis / (1000 * 60)).toInt()
        val totalHours = totalMinutes / 60
        return Triple(totalHours / 24, totalHours % 24, totalMinutes % 60)
    }

    fun clearAdRemovalTime() {
        sp.remove(KEY_AD_REMOVAL_END_TIME)
        Log.d(TAG, "已清除去广告时间")
        notifyAdRemovalChanged()
    }

    private val videoTaskRewards = listOf(6, 18, 18, 12, 12, 6, 24)

    private fun checkAndResetDailyTasks() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastResetDate = sp.getString(KEY_VIDEO_TASKS_RESET_DATE, "")
        if (lastResetDate != today) {
            for (i in 0 until 7) sp.remove("${KEY_VIDEO_TASK_COMPLETED}$i")
            sp.putString(KEY_VIDEO_TASKS_RESET_DATE, today)
            Log.d(TAG, "视频任务已重置，日期: $today")
        }
    }

    fun getCurrentAvailableVideoTask(): Pair<Int, Int>? {
        checkAndResetDailyTasks()
        for (i in 0 until 7) {
            if (!sp.getBoolean("${KEY_VIDEO_TASK_COMPLETED}$i", false)) {
                return Pair(i, videoTaskRewards[i])
            }
        }
        return null
    }

    fun claimVideoTaskReward(): Pair<Boolean, Int>? {
        val taskInfo = getCurrentAvailableVideoTask() ?: return Pair(false, 0)
        val (taskIndex, rewardHours) = taskInfo
        sp.putBoolean("${KEY_VIDEO_TASK_COMPLETED}$taskIndex", true)
        updateContinuousDays()
        addAdRemovalTime(rewardHours)
        Log.d(TAG, "成功领取视频任务奖励: 任务${taskIndex + 1}, 奖励${rewardHours}小时")
        return Pair(true, rewardHours)
    }

    private fun updateContinuousDays() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = sp.getString(KEY_LAST_VIDEO_DATE, "")
        val currentDays = sp.getInt(KEY_CONTINUOUS_DAYS, 0)
        when {
            lastDate.isEmpty() -> {
                sp.putInt(KEY_CONTINUOUS_DAYS, 1)
                sp.putString(KEY_LAST_VIDEO_DATE, today)
            }
            lastDate == today -> {}
            else -> {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val daysDiff = ((dateFormat.parse(today)!!.time - dateFormat.parse(lastDate)!!.time) / (1000 * 60 * 60 * 24)).toInt()
                    when {
                        daysDiff == 1 -> {
                            sp.putInt(KEY_CONTINUOUS_DAYS, currentDays + 1)
                            sp.putString(KEY_LAST_VIDEO_DATE, today)
                        }
                        daysDiff > 1 -> {
                            sp.putInt(KEY_CONTINUOUS_DAYS, 1)
                            sp.putString(KEY_LAST_VIDEO_DATE, today)
                        }
                    }
                } catch (e: Exception) {
                    sp.putInt(KEY_CONTINUOUS_DAYS, 1)
                    sp.putString(KEY_LAST_VIDEO_DATE, today)
                }
            }
        }
    }

    fun getContinuousDays(): Int {
        resetDailyRewardsIfNeeded()
        return sp.getInt(KEY_CONTINUOUS_DAYS, 0).coerceIn(0, 7)
    }

    fun isDailyRewardClaimed(dayIndex: Int): Boolean =
        dayIndex in 0..6 && sp.getBoolean("${KEY_DAILY_REWARD_CLAIMED}$dayIndex", false)

    fun markDailyRewardClaimed(dayIndex: Int) {
        if (dayIndex in 0..6) {
            sp.putBoolean("${KEY_DAILY_REWARD_CLAIMED}$dayIndex", true)
        }
    }

    private fun resetDailyRewardsIfNeeded() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastResetDate = sp.getString("daily_rewards_reset_date", "")
        if (lastResetDate != today) {
            for (i in 0 until 7) sp.remove("${KEY_DAILY_REWARD_CLAIMED}$i")
            sp.putString("daily_rewards_reset_date", today)
        }
    }

    fun isVideoTaskCompleted(taskIndex: Int): Boolean =
        taskIndex in 0..6 && sp.getBoolean("${KEY_VIDEO_TASK_COMPLETED}$taskIndex", false)

    fun getVideoTaskReward(taskIndex: Int): Int =
        if (taskIndex in 0..6) videoTaskRewards[taskIndex] else 0
}
