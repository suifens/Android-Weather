package com.goodtech.tq.utils

import android.util.Log

/**
 * 去广告时间管理工具类
 * 负责保存和检查去广告状态
 */
object AdRemovalManager {
    private const val TAG = "AdRemovalManager"
    
    // SharedPreferences key
    private const val KEY_AD_REMOVAL_END_TIME = "ad_removal_end_time" // 去广告结束时间戳（毫秒）
    private const val KEY_VIDEO_TASK_COMPLETED = "video_task_completed_" // 视频任务完成状态 key前缀
    private const val KEY_VIDEO_TASKS_RESET_DATE = "video_tasks_reset_date" // 视频任务重置日期
    private const val KEY_CONTINUOUS_DAYS = "continuous_video_days" // 连续观看视频天数
    private const val KEY_LAST_VIDEO_DATE = "last_video_watch_date" // 最后观看视频日期
    private const val KEY_DAILY_REWARD_CLAIMED = "daily_reward_claimed_" // 每日奖励领取状态 key前缀
    
    /**
     * 添加去广告时长
     * @param hours 小时数（默认24小时，即1天）
     * 如果已有去广告时长，会在现有基础上累加
     */
    fun addAdRemovalTime(hours: Int = 24) {
        val currentTime = System.currentTimeMillis()
        val existingEndTime = SpUtils.getInstance().getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        
        // 如果已有去广告时长且未过期，在现有结束时间基础上累加
        // 如果已过期或没有，从当前时间开始计算
        val baseTime = if (existingEndTime > currentTime) {
            existingEndTime
        } else {
            currentTime
        }
        
        val endTime = baseTime + (hours * 60 * 60 * 1000L)
        
        SpUtils.getInstance().putLong(KEY_AD_REMOVAL_END_TIME, endTime)
        Log.d(TAG, "添加去广告时长: ${hours}小时，结束时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(endTime))}")
    }
    
    /**
     * 检查是否在去广告有效期内
     * @return true 如果在有效期内，false 如果已过期或未设置
     */
    fun isAdRemovalActive(): Boolean {
        val endTime = SpUtils.getInstance().getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        if (endTime == 0L) {
            return false
        }
        
        val currentTime = System.currentTimeMillis()
        val isActive = currentTime < endTime
        
        if (!isActive && endTime > 0) {
            // 已过期，清除过期数据
            SpUtils.getInstance().remove(KEY_AD_REMOVAL_END_TIME)
            Log.d(TAG, "去广告时间已过期，已清除")
        }
        
        return isActive
    }
    
    /**
     * 获取剩余去广告时长（毫秒）
     * @return 剩余时长，如果已过期或未设置则返回0
     */
    fun getRemainingTimeMillis(): Long {
        val endTime = SpUtils.getInstance().getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        if (endTime == 0L) {
            return 0L
        }
        
        val currentTime = System.currentTimeMillis()
        val remaining = endTime - currentTime
        
        return if (remaining > 0) remaining else 0L
    }
    
    /**
     * 获取剩余去广告时长（天和小时）
     * 仅用于兼容旧逻辑，如果需要分钟请使用 getRemainingTimeDetail
     * @return Pair<天数, 小时数>
     */
    fun getRemainingTime(): Pair<Int, Int> {
        val (days, hours, _) = getRemainingTimeDetail()
        return Pair(days, hours)
    }
    
    /**
     * 获取剩余去广告时长（天、小时、分钟）
     * @return Triple<天数, 小时数, 分钟数>
     */
    fun getRemainingTimeDetail(): Triple<Int, Int, Int> {
        val remainingMillis = getRemainingTimeMillis()
        if (remainingMillis <= 0) {
            return Triple(0, 0, 0)
        }
        
        val totalMinutes = (remainingMillis / (1000 * 60)).toInt()
        val totalHours = totalMinutes / 60
        val days = totalHours / 24
        val hours = totalHours % 24
        val minutes = totalMinutes % 60
        
        return Triple(days, hours, minutes)
    }
    
    /**
     * 清除去广告时间
     */
    fun clearAdRemovalTime() {
        SpUtils.getInstance().remove(KEY_AD_REMOVAL_END_TIME)
        Log.d(TAG, "已清除去广告时间")
    }
    
    /**
     * 视频任务奖励时长配置（对应任务1-7）
     */
    private val videoTaskRewards = listOf(6, 18, 18, 12, 12, 12, 24) // 小时数
    
    /**
     * 检查并重置每日视频任务（如果需要）
     * 如果日期变化，重置所有任务状态
     */
    private fun checkAndResetDailyTasks() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastResetDate = SpUtils.getInstance().getString(KEY_VIDEO_TASKS_RESET_DATE, "")
        
        if (lastResetDate != today) {
            // 日期变化，重置所有任务（清除完成状态，第一个任务默认可用）
            for (i in 0 until 7) {
                SpUtils.getInstance().remove("${KEY_VIDEO_TASK_COMPLETED}${i}")
            }
            SpUtils.getInstance().putString(KEY_VIDEO_TASKS_RESET_DATE, today)
            Log.d(TAG, "视频任务已重置，日期: $today")
        }
    }
    
    /**
     * 获取当前可领取的视频任务索引和奖励时长
     * @return Pair<任务索引(0-6), 奖励时长(小时)>，如果没有可领取的任务则返回 null
     */
    fun getCurrentAvailableVideoTask(): Pair<Int, Int>? {
        checkAndResetDailyTasks()
        
        // 查找第一个未完成的任务
        for (i in 0 until 7) {
            val isCompleted = SpUtils.getInstance().getBoolean("${KEY_VIDEO_TASK_COMPLETED}${i}", false)
            if (!isCompleted) {
                val rewardHours = videoTaskRewards[i]
                Log.d(TAG, "找到可领取的视频任务: 任务${i + 1}, 奖励${rewardHours}小时")
                return Pair(i, rewardHours)
            }
        }
        
        Log.d(TAG, "所有视频任务已完成")
        return null
    }
    
    /**
     * 领取视频任务奖励
     * 在 RemoveAdActivity 中看完视频后调用
     * @return Pair<是否成功, 奖励时长>，如果成功返回奖励时长，失败返回 null
     */
    fun claimVideoTaskReward(): Pair<Boolean, Int>? {
        val taskInfo = getCurrentAvailableVideoTask()
        if (taskInfo == null) {
            Log.w(TAG, "没有可领取的视频任务")
            return Pair(false, 0)
        }
        
        val (taskIndex, rewardHours) = taskInfo
        
        // 标记当前任务为已完成
        SpUtils.getInstance().putBoolean("${KEY_VIDEO_TASK_COMPLETED}${taskIndex}", true)
        
        // 更新连续观看天数
        updateContinuousDays()
        
        // 增加去广告时长
        addAdRemovalTime(rewardHours)
        
        Log.d(TAG, "成功领取视频任务奖励: 任务${taskIndex + 1}, 奖励${rewardHours}小时")
        return Pair(true, rewardHours)
    }
    
    /**
     * 更新连续观看视频天数
     * 根据最后观看日期和当前日期来判断是否连续
     */
    private fun updateContinuousDays() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastDate = SpUtils.getInstance().getString(KEY_LAST_VIDEO_DATE, "")
        val currentDays = SpUtils.getInstance().getInt(KEY_CONTINUOUS_DAYS, 0)
        
        if (lastDate.isEmpty()) {
            // 第一次观看
            SpUtils.getInstance().putInt(KEY_CONTINUOUS_DAYS, 1)
            SpUtils.getInstance().putString(KEY_LAST_VIDEO_DATE, today)
            Log.d(TAG, "第一次观看视频，连续天数: 1")
        } else if (lastDate == today) {
            // 今天已经观看过，不重复计算
            Log.d(TAG, "今天已观看过视频，连续天数: $currentDays")
        } else {
            // 计算日期差
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            try {
                val lastDateObj = dateFormat.parse(lastDate)
                val todayDateObj = dateFormat.parse(today)
                if (lastDateObj != null && todayDateObj != null) {
                    val daysDiff = ((todayDateObj.time - lastDateObj.time) / (1000 * 60 * 60 * 24)).toInt()
                    
                    if (daysDiff == 1) {
                        // 连续观看：昨天观看，今天继续
                        val newDays = currentDays + 1
                        SpUtils.getInstance().putInt(KEY_CONTINUOUS_DAYS, newDays)
                        SpUtils.getInstance().putString(KEY_LAST_VIDEO_DATE, today)
                        Log.d(TAG, "连续观看视频，连续天数: $newDays")
                    } else if (daysDiff > 1) {
                        // 中断了，重新开始
                        SpUtils.getInstance().putInt(KEY_CONTINUOUS_DAYS, 1)
                        SpUtils.getInstance().putString(KEY_LAST_VIDEO_DATE, today)
                        Log.d(TAG, "观看中断，重新开始，连续天数: 1")
                    } else {
                        // 日期异常，保持原样
                        Log.w(TAG, "日期异常，保持连续天数: $currentDays")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "日期解析失败: ${e.message}", e)
                // 出错时重置
                SpUtils.getInstance().putInt(KEY_CONTINUOUS_DAYS, 1)
                SpUtils.getInstance().putString(KEY_LAST_VIDEO_DATE, today)
            }
        }
    }
    
    /**
     * 获取连续观看视频天数
     * @return 连续天数（1-7）
     */
    fun getContinuousDays(): Int {
        // 检查并重置每日奖励状态（如果需要）
        resetDailyRewardsIfNeeded()
        
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastDate = SpUtils.getInstance().getString(KEY_LAST_VIDEO_DATE, "")
        
        // 如果今天还没观看，返回当前连续天数（可能是昨天或之前的）
        // 如果今天已观看，返回更新后的连续天数
        return SpUtils.getInstance().getInt(KEY_CONTINUOUS_DAYS, 0).coerceIn(0, 7)
    }
    
    /**
     * 检查每日奖励是否已领取
     * @param dayIndex 天数索引（0-6，对应第1-7天）
     * @return true 如果已领取，false 如果未领取
     */
    fun isDailyRewardClaimed(dayIndex: Int): Boolean {
        if (dayIndex < 0 || dayIndex >= 7) {
            return false
        }
        return SpUtils.getInstance().getBoolean("${KEY_DAILY_REWARD_CLAIMED}${dayIndex}", false)
    }
    
    /**
     * 标记每日奖励为已领取
     * @param dayIndex 天数索引（0-6，对应第1-7天）
     */
    fun markDailyRewardClaimed(dayIndex: Int) {
        if (dayIndex >= 0 && dayIndex < 7) {
            SpUtils.getInstance().putBoolean("${KEY_DAILY_REWARD_CLAIMED}${dayIndex}", true)
            Log.d(TAG, "标记每日奖励已领取: 第${dayIndex + 1}天")
        }
    }
    
    /**
     * 重置每日奖励状态（每天重置）
     */
    private fun resetDailyRewardsIfNeeded() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastResetDate = SpUtils.getInstance().getString("daily_rewards_reset_date", "")
        
        if (lastResetDate != today) {
            // 清除所有每日奖励的领取状态
            for (i in 0 until 7) {
                SpUtils.getInstance().remove("${KEY_DAILY_REWARD_CLAIMED}${i}")
            }
            SpUtils.getInstance().putString("daily_rewards_reset_date", today)
            Log.d(TAG, "每日奖励状态已重置，日期: $today")
        }
    }
    
    /**
     * 获取视频任务完成状态
     * @param taskIndex 任务索引（0-6）
     * @return true 如果已完成，false 如果未完成
     */
    fun isVideoTaskCompleted(taskIndex: Int): Boolean {
        if (taskIndex < 0 || taskIndex >= 7) {
            return false
        }
        return SpUtils.getInstance().getBoolean("${KEY_VIDEO_TASK_COMPLETED}${taskIndex}", false)
    }
    
    /**
     * 获取视频任务的奖励时长
     * @param taskIndex 任务索引（0-6）
     * @return 奖励时长（小时）
     */
    fun getVideoTaskReward(taskIndex: Int): Int {
        if (taskIndex < 0 || taskIndex >= 7) {
            return 0
        }
        return videoTaskRewards[taskIndex]
    }
}
