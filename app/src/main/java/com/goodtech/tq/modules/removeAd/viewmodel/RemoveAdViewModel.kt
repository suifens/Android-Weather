package com.goodtech.tq.modules.removeAd.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.goodtech.tq.modules.removeAd.model.DailyReward
import com.goodtech.tq.modules.removeAd.model.DailyRewardStatus
import com.goodtech.tq.modules.removeAd.model.VideoTask
import com.goodtech.tq.modules.removeAd.model.VideoTaskStatus
import com.goodtech.tq.utils.AdRemovalManager

/**
 * 去广告页面ViewModel
 * 负责管理去广告相关的业务逻辑和数据状态
 * 包括：剩余时长管理、视频任务状态、每日奖励状态等
 */
class RemoveAdViewModel : ViewModel() {

    // LiveData用于观察数据变化
    /** 剩余时长LiveData，格式：剩余: XX天XX小时 */
    val remainingTimeLiveData = MutableLiveData<String>()
    /** 视频任务列表LiveData */
    val videoTasksLiveData = MutableLiveData<List<VideoTask>>()
    /** 每日奖励列表LiveData */
    val dailyRewardsLiveData = MutableLiveData<List<DailyReward>>()

    // 当前剩余时长数据
    private var remainingDays = 0
    private var remainingHours = 0

    /**
     * 加载去广告相关数据
     * 初始化剩余时长、视频任务和每日奖励数据
     */
    fun loadAdRemovalData() {
        // 从 AdRemovalManager 加载当前去广告状态
        val (days, hours) = AdRemovalManager.getRemainingTime()
        remainingDays = days
        remainingHours = hours
        
        // 更新剩余时长显示
        updateRemainingTime()
        loadVideoTasks()
        loadDailyRewards()
    }

    /**
     * 更新剩余时长显示
     * 将天数和小时数格式化为显示文本
     */
    private fun updateRemainingTime() {
        val timeText = "剩余: ${String.format("%02d", remainingDays)}天${String.format("%02d", remainingHours)}小时"
        remainingTimeLiveData.value = timeText
    }

    /**
     * 加载视频任务数据
     * 初始化7个视频任务的状态和奖励时长
     */
    private fun loadVideoTasks() {
        val tasks = listOf(
            VideoTask(1, 6, VideoTaskStatus.COMPLETED),    // 第1个视频：6小时，已领取
            VideoTask(2, 18, VideoTaskStatus.AVAILABLE),   // 第2个视频：18小时，可领取
            VideoTask(3, 18, VideoTaskStatus.LOCKED),      // 第3个视频：18小时，未解锁
            VideoTask(4, 12, VideoTaskStatus.LOCKED),      // 第4个视频：12小时，未解锁
            VideoTask(5, 12, VideoTaskStatus.LOCKED),      // 第5个视频：12小时，未解锁
            VideoTask(6, 12, VideoTaskStatus.LOCKED),      // 第6个视频：12小时，未解锁
            VideoTask(7, 24, VideoTaskStatus.LOCKED)       // 第7个视频：24小时，未解锁
        )
        videoTasksLiveData.value = tasks
    }

    /**
     * 加载每日奖励数据
     * 初始化7天连续签到的状态
     */
    private fun loadDailyRewards() {
        val rewards = listOf(
            DailyReward(1, DailyRewardStatus.CURRENT),     // 第1天：当前天
            DailyReward(2, DailyRewardStatus.LOCKED),      // 第2天：未解锁
            DailyReward(3, DailyRewardStatus.LOCKED),      // 第3天：未解锁
            DailyReward(4, DailyRewardStatus.LOCKED),      // 第4天：未解锁
            DailyReward(5, DailyRewardStatus.LOCKED),      // 第5天：未解锁
            DailyReward(6, DailyRewardStatus.LOCKED),      // 第6天：未解锁
            DailyReward(7, DailyRewardStatus.LOCKED)       // 第7天：未解锁
        )
        dailyRewardsLiveData.value = rewards
    }

    /**
     * 领取视频奖励
     * @param taskIndex 任务索引（0-6）
     * @param rewardHours 奖励时长（小时），默认24小时（1天）
     */
    fun claimVideoReward(taskIndex: Int, rewardHours: Int = 24) {
        val currentTasks = videoTasksLiveData.value?.toMutableList() ?: return
        
        // 检查任务是否可领取
        if (taskIndex < currentTasks.size && currentTasks[taskIndex].status == VideoTaskStatus.AVAILABLE) {
            // 标记任务为已完成
            currentTasks[taskIndex] = currentTasks[taskIndex].copy(status = VideoTaskStatus.COMPLETED)
            
            // 保存去广告时间到 AdRemovalManager（默认1天）
            AdRemovalManager.addAdRemovalTime(rewardHours)
            
            // 重新加载剩余时长
            val (days, hours) = AdRemovalManager.getRemainingTime()
            remainingDays = days
            remainingHours = hours
            
            // 解锁下一个任务（如果存在）
            if (taskIndex + 1 < currentTasks.size) {
                currentTasks[taskIndex + 1] = currentTasks[taskIndex + 1].copy(status = VideoTaskStatus.AVAILABLE)
            }
            
            // 更新UI
            videoTasksLiveData.value = currentTasks
            updateRemainingTime()
        }
    }

    /**
     * 领取每日奖励
     * @param dayIndex 天数索引（0-6）
     */
    fun claimDailyReward(dayIndex: Int) {
        val currentRewards = dailyRewardsLiveData.value?.toMutableList() ?: return
        
        // 检查是否为当前天
        if (dayIndex < currentRewards.size && currentRewards[dayIndex].status == DailyRewardStatus.CURRENT) {
            // 标记当前天为已完成
            currentRewards[dayIndex] = currentRewards[dayIndex].copy(status = DailyRewardStatus.COMPLETED)
            
            // 解锁下一天（如果存在）
            if (dayIndex + 1 < currentRewards.size) {
                currentRewards[dayIndex + 1] = currentRewards[dayIndex + 1].copy(status = DailyRewardStatus.CURRENT)
            }
            
            // 更新UI
            dailyRewardsLiveData.value = currentRewards
        }
    }

    /**
     * 续时长操作
     * 处理用户点击续时长按钮的逻辑
     * 可以打开购买对话框或显示续费选项
     */
    fun renewDuration() {
        // 处理续时长操作
        // 这里可以打开购买对话框或显示续费选项
    }
}
