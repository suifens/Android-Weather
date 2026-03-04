package com.chunjing.tq.modules.removeAd.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chunjing.tq.modules.removeAd.model.DailyReward
import com.chunjing.tq.modules.removeAd.model.DailyRewardStatus
import com.chunjing.tq.modules.removeAd.model.VideoTask
import com.chunjing.tq.modules.removeAd.model.VideoTaskStatus
import com.chunjing.tq.utils.AdRemovalManager
import com.goodtech.weatherlib.utils.SpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemoveAdViewModel : ViewModel() {
    val remainingDayLiveData = MutableLiveData<String>()
    val remainingHourLiveData = MutableLiveData<String>()
    val remainingDayUnitLiveData = MutableLiveData<String>()
    val remainingHourUnitLiveData = MutableLiveData<String>()
    val videoTasksLiveData = MutableLiveData<List<VideoTask>>()
    val dailyRewardsLiveData = MutableLiveData<List<DailyReward>>()

    private var remainingDays = 0
    private var remainingHours = 0
    private var remainingMinutes = 0

    fun loadAdRemovalData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val (d, h, m) = AdRemovalManager.getRemainingTimeDetail()
                remainingDays = d
                remainingHours = h
                remainingMinutes = m
            }
            updateRemainingTime()
        }
        loadVideoTasks()
        loadDailyRewards()
    }

    private fun updateRemainingTime() {
        if (remainingDays >= 1) {
            remainingDayLiveData.value = "%02d".format(remainingDays)
            remainingHourLiveData.value = "%02d".format(remainingHours)
            remainingDayUnitLiveData.value = "天"
            remainingHourUnitLiveData.value = "小时"
        } else {
            remainingDayLiveData.value = "%02d".format(remainingHours)
            remainingHourLiveData.value = "%02d".format(remainingMinutes)
            remainingDayUnitLiveData.value = "小时"
            remainingHourUnitLiveData.value = "分钟"
        }
    }

    private fun loadVideoTasks() {
        viewModelScope.launch(Dispatchers.Default) {
            val rewards = listOf(6, 18, 18, 12, 12, 6, 24)
            val currentTask = AdRemovalManager.getCurrentAvailableVideoTask()
            val availableTaskIndex = currentTask?.first ?: -1
            val tasks = (0 until 7).map { i ->
                val isCompleted = AdRemovalManager.isVideoTaskCompleted(i)
                val status = when {
                    isCompleted -> VideoTaskStatus.COMPLETED
                    i == availableTaskIndex -> VideoTaskStatus.AVAILABLE
                    i < availableTaskIndex -> VideoTaskStatus.COMPLETED
                    else -> VideoTaskStatus.LOCKED
                }
                VideoTask(i + 1, rewards[i], status)
            }
            withContext(Dispatchers.Main) { videoTasksLiveData.value = tasks }
        }
    }

    private fun loadDailyRewards() {
        viewModelScope.launch(Dispatchers.Default) {
            val continuousDays = AdRemovalManager.getContinuousDays()
            val rewards = (0 until 7).map { dayIndex ->
                val dayNumber = dayIndex + 1
                val isClaimed = AdRemovalManager.isDailyRewardClaimed(dayIndex)
                val status = when {
                    isClaimed -> DailyRewardStatus.COMPLETED
                    dayNumber <= continuousDays -> DailyRewardStatus.CURRENT
                    else -> DailyRewardStatus.LOCKED
                }
                DailyReward(dayNumber, status)
            }
            withContext(Dispatchers.Main) { dailyRewardsLiveData.value = rewards }
        }
    }

    fun claimVideoReward(taskIndex: Int, rewardHours: Int) {
        val currentTasks = videoTasksLiveData.value?.toMutableList() ?: return
        if (taskIndex < currentTasks.size && currentTasks[taskIndex].status == VideoTaskStatus.AVAILABLE) {
            SpUtils.instance.putBoolean("video_task_completed_${taskIndex}", true)
            AdRemovalManager.addAdRemovalTime(rewardHours)
            val (d, h, m) = AdRemovalManager.getRemainingTimeDetail()
            remainingDays = d
            remainingHours = h
            remainingMinutes = m
            loadVideoTasks()
            updateRemainingTime()
        }
    }

    fun claimDailyReward(dayIndex: Int) {
        val currentRewards = dailyRewardsLiveData.value?.toMutableList() ?: return
        if (dayIndex < currentRewards.size && currentRewards[dayIndex].status == DailyRewardStatus.CURRENT) {
            val continuousDays = AdRemovalManager.getContinuousDays()
            val dayNumber = dayIndex + 1
            if (dayNumber <= continuousDays) {
                AdRemovalManager.markDailyRewardClaimed(dayIndex)
                val rewardDays = when {
                    dayNumber <= 3 -> 0
                    dayNumber <= 6 -> 0
                    else -> 7
                }
                AdRemovalManager.addAdRemovalTime(rewardDays)
                val (d, h, m) = AdRemovalManager.getRemainingTimeDetail()
                remainingDays = d
                remainingHours = h
                remainingMinutes = m
                loadDailyRewards()
                updateRemainingTime()
            }
        }
    }
}
