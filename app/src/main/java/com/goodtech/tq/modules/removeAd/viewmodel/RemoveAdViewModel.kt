package com.goodtech.tq.modules.removeAd.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodtech.tq.modules.removeAd.model.DailyReward
import com.goodtech.tq.modules.removeAd.model.DailyRewardStatus
import com.goodtech.tq.modules.removeAd.model.VideoTask
import com.goodtech.tq.modules.removeAd.model.VideoTaskStatus
import com.goodtech.tq.utils.AdRemovalManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 去广告页面ViewModel
 * 负责管理去广告相关的业务逻辑和数据状态
 * 包括：剩余时长管理、视频任务状态、每日奖励状态等
 */
@SuppressLint("DefaultLocale")
class RemoveAdViewModel : ViewModel() {

    // LiveData用于观察数据变化
    /** 剩余时长LiveData，格式：剩余: XX天XX小时 */
    val remainingDayLiveData = MutableLiveData<String>()
    val remainingHourLiveData = MutableLiveData<String>()
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
     * 使用协程异步加载，不阻塞主线程
     */
    fun loadAdRemovalData() {
        viewModelScope.launch {
            // 在后台线程加载数据
            withContext(Dispatchers.IO) {
                // 从 AdRemovalManager 加载当前去广告状态
                val (days, hours) = AdRemovalManager.getRemainingTime()
                remainingDays = days
                remainingHours = hours
            }
            
            // 在主线程更新UI
            updateRemainingTime()
        }
        
        // 并行加载视频任务和每日奖励（不阻塞）
        loadVideoTasks()
        loadDailyRewards()
    }

    /**
     * 更新剩余时长显示
     * 将天数和小时数格式化为显示文本
     */
    private fun updateRemainingTime() {
        remainingDayLiveData.value = String.format("%02d", remainingDays)
        remainingHourLiveData.value = String.format("%02d", remainingHours)
    }

    /**
     * 加载视频任务数据
     * 从 AdRemovalManager 读取任务状态，初始化7个视频任务的状态和奖励时长
     * 优化：缓存奖励配置，减少重复计算
     */
    private fun loadVideoTasks() {
        viewModelScope.launch(Dispatchers.Default) {
            val tasks = mutableListOf<VideoTask>()
            
            // 视频任务奖励时长配置（对应任务1-7）- 缓存配置
            val rewards = listOf(6, 18, 18, 12, 12, 6, 24)
            
            // 查找第一个可领取的任务索引（批量检查，减少 SharedPreferences 访问）
            val currentTask = AdRemovalManager.getCurrentAvailableVideoTask()
            val availableTaskIndex = currentTask?.first ?: -1
            
            // 批量检查任务完成状态
            val completedStatus = BooleanArray(7) { i ->
                AdRemovalManager.isVideoTaskCompleted(i)
            }
            
            for (i in 0 until 7) {
                val taskNumber = i + 1
                val rewardHours = rewards[i]
                val isCompleted = completedStatus[i]
                
                val status = when {
                    isCompleted -> VideoTaskStatus.COMPLETED  // 已完成
                    i == availableTaskIndex -> VideoTaskStatus.AVAILABLE  // 当前可领取
                    i < availableTaskIndex -> VideoTaskStatus.COMPLETED  // 已完成（之前的任务）
                    else -> VideoTaskStatus.LOCKED  // 未解锁（后续任务）
                }
                
                tasks.add(VideoTask(taskNumber, rewardHours, status))
            }
            
            // 在主线程更新 LiveData
            withContext(Dispatchers.Main) {
                videoTasksLiveData.value = tasks
            }
        }
    }

    /**
     * 加载每日奖励数据
     * 根据连续观看视频天数来判断每日奖励状态
     * 优化：批量检查状态，减少 SharedPreferences 访问
     */
    private fun loadDailyRewards() {
        viewModelScope.launch(Dispatchers.Default) {
            // 获取连续观看天数（会自动处理每日重置）
            val continuousDays = AdRemovalManager.getContinuousDays()
            
            // 批量检查每日奖励领取状态
            val claimedStatus = BooleanArray(7) { dayIndex ->
                AdRemovalManager.isDailyRewardClaimed(dayIndex)
            }
            
            val rewards = mutableListOf<DailyReward>()
            
            for (dayIndex in 0 until 7) {
                val dayNumber = dayIndex + 1
                val isClaimed = claimedStatus[dayIndex]
                
                val status = when {
                    isClaimed -> {
                        // 已领取
                        DailyRewardStatus.COMPLETED
                    }
                    dayNumber <= continuousDays -> {
                        // 当前可领取：连续天数已达到或超过该天数
                        DailyRewardStatus.CURRENT
                    }
                    else -> {
                        // 未解锁：连续天数还未达到
                        DailyRewardStatus.LOCKED
                    }
                }
                
                rewards.add(DailyReward(dayNumber, status))
            }
            
            // 在主线程更新 LiveData
            withContext(Dispatchers.Main) {
                dailyRewardsLiveData.value = rewards
            }
        }
    }

    /**
     * 领取视频奖励
     * @param taskIndex 任务索引（0-6）
     * @param rewardHours 奖励时长（小时）
     * 注意：此方法现在主要用于 RemoveAdActivity 中的手动领取
     * DrawDramaFragment 中的自动领取会直接调用 AdRemovalManager.claimVideoTaskReward()
     */
    fun claimVideoReward(taskIndex: Int, rewardHours: Int) {
        val currentTasks = videoTasksLiveData.value?.toMutableList() ?: return
        
        // 检查任务是否可领取
        if (taskIndex < currentTasks.size && currentTasks[taskIndex].status == VideoTaskStatus.AVAILABLE) {
            // 标记任务为已完成（同步到 AdRemovalManager，使用相同的 key 格式）
            com.goodtech.tq.utils.SpUtils.getInstance().putBoolean("video_task_completed_${taskIndex}", true)
            
            // 保存去广告时间到 AdRemovalManager
            AdRemovalManager.addAdRemovalTime(rewardHours)
            
            // 重新加载剩余时长
            val (days, hours) = AdRemovalManager.getRemainingTime()
            remainingDays = days
            remainingHours = hours
            
            // 重新加载任务列表以同步状态
            loadVideoTasks()
            updateRemainingTime()
        }
    }

    /**
     * 领取每日奖励
     * @param dayIndex 天数索引（0-6）
     * 根据连续观看天数来判断是否可以领取
     */
    fun claimDailyReward(dayIndex: Int) {
        val currentRewards = dailyRewardsLiveData.value?.toMutableList() ?: return
        
        // 检查是否为当前可领取的天数
        if (dayIndex < currentRewards.size && currentRewards[dayIndex].status == DailyRewardStatus.CURRENT) {
            // 获取连续观看天数
            val continuousDays = AdRemovalManager.getContinuousDays()
            val dayNumber = dayIndex + 1
            
            // 检查连续天数是否达到要求
            if (dayNumber <= continuousDays) {
                // 标记为已领取
                AdRemovalManager.markDailyRewardClaimed(dayIndex)
                
                // 根据天数给予对应的去广告时长奖励
                // 第1-3天：各1天（24小时）
                // 第4-6天：各2天（48小时）
                // 第7天：7-100天随机（这里简化为7天，可以根据需求调整）
                val rewardDays = when {
                    dayNumber <= 3 -> 0  // 1-3天：1天
                    dayNumber <= 6 -> 0  // 4-6天：2天
                    else -> 7  // 第7天：7天（可以改为随机7-100天）
                }
                
                // 增加去广告时长
//                AdRemovalManager.addAdRemovalTime(rewardDays * 24)
                AdRemovalManager.addAdRemovalTime(rewardDays)
                
                // 重新加载剩余时长
                val (days, hours) = AdRemovalManager.getRemainingTime()
                remainingDays = days
                remainingHours = hours
                
                // 重新加载奖励列表以更新UI
                loadDailyRewards()
                updateRemainingTime()
            }
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
