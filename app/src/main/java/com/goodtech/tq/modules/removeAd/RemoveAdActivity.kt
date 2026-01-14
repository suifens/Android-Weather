package com.goodtech.tq.modules.removeAd

import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.BarUtils
import com.gengee.insaitlib.ui.base.BaseVmActivity
import com.goodtech.tq.R
import com.goodtech.tq.databinding.ActivityRemoveAdBinding
import com.goodtech.tq.modules.removeAd.model.DailyReward
import com.goodtech.tq.modules.removeAd.model.DailyRewardStatus
import com.goodtech.tq.modules.removeAd.model.VideoTask
import com.goodtech.tq.modules.removeAd.model.VideoTaskStatus
import com.goodtech.tq.modules.removeAd.viewmodel.RemoveAdViewModel

/**
 * 去广告页面Activity
 * 功能：用户通过观看视频获得去广告时长，包含视频任务和连续签到奖励
 * 架构：使用MVVM模式，ViewBinding进行视图绑定
 */
class RemoveAdActivity : BaseVmActivity<ActivityRemoveAdBinding, RemoveAdViewModel>() {

    companion object {
        /**
         * 启动去广告页面的静态方法
         * @param context 上下文
         */
        @JvmStatic
        fun startActivity(context: Context) {
            val intent = Intent(context, RemoveAdActivity::class.java)
            context.startActivity(intent)
        }
    }

    /**
     * 准备数据，当前页面无需额外数据
     */
    override fun prepareData(intent: Intent?) {
        // No extra data needed for this activity
    }

    /**
     * 绑定视图，使用ViewBinding
     */
    override fun bindView() = ActivityRemoveAdBinding.inflate(layoutInflater)

    /**
     * 初始化视图
     * 配置状态栏、设置视频任务和每日奖励的初始状态
     */
    override fun initView() {
        // 配置状态栏适配
        configStationBar(mBinding.privateStationBar)
        // 设置状态栏为浅色模式（黑色文字）
        BarUtils.setStatusBarLightMode(this, true)
        
        // 初始化视频任务视图
        setupVideoTasks()
        // 初始化每日奖励视图
        setupDailyRewards()
    }

    /**
     * 初始化事件监听器
     * 设置返回按钮、续时长按钮、视频任务和每日奖励的点击事件
     */
    override fun initEvent() {
        // 返回按钮点击事件
        mBinding.buttonBack.setOnClickListener { finish() }
        
        // 续时长按钮点击事件
        mBinding.renewButton.setOnClickListener { 
            // 处理续时长操作
            viewModel.renewDuration()
        }
        
        // 设置视频任务点击监听器
        setupVideoTaskListeners()
        
        // 设置每日奖励点击监听器
        setupDailyRewardListeners()
    }

    /**
     * 初始化数据
     * 加载去广告数据并观察数据变化
     */
    override fun initData() {
        // 加载去广告相关数据
        viewModel.loadAdRemovalData()
        
        // 观察剩余时长数据变化
        viewModel.remainingTimeLiveData.observe(this) { remainingTime ->
            mBinding.remainingTimeText.text = remainingTime
        }
        
        // 观察视频任务数据变化
        viewModel.videoTasksLiveData.observe(this) { tasks ->
            updateVideoTasks(tasks)
        }
        
        // 观察每日奖励数据变化
        viewModel.dailyRewardsLiveData.observe(this) { rewards ->
            updateDailyRewards(rewards)
        }
    }

    /**
     * 设置视频任务视图
     * 初始化7个视频任务的显示文本
     */
    private fun setupVideoTasks() {
        // 获取所有视频任务视图
        val videoTasks = listOf(
            mBinding.videoTask1, mBinding.videoTask2, mBinding.videoTask3, mBinding.videoTask4,
            mBinding.videoTask5, mBinding.videoTask6, mBinding.videoTask7
        )
        
        // 为每个视频任务设置编号文本
        videoTasks.forEachIndexed { index, taskView ->
            taskView.videoNumberText.text = "第${index + 1}个视频"
        }
    }

    /**
     * 设置每日奖励视图
     * 初始化7天连续签到的显示文本
     */
    private fun setupDailyRewards() {
        // 获取所有每日奖励按钮
        val dailyRewards = listOf(
            mBinding.day1Button, mBinding.day2Button, mBinding.day3Button, mBinding.day4Button,
            mBinding.day5Button, mBinding.day6Button, mBinding.day7Button
        )
        
        // 为每个天数按钮设置文本
        dailyRewards.forEachIndexed { index, dayButton ->
            dayButton.text = "第${index + 1}天"
        }
    }

    /**
     * 设置视频任务点击监听器
     * 为每个视频任务的领取按钮设置点击事件
     */
    private fun setupVideoTaskListeners() {
        // 获取所有视频任务视图
        val videoTasks = listOf(
            mBinding.videoTask1, mBinding.videoTask2, mBinding.videoTask3, mBinding.videoTask4,
            mBinding.videoTask5, mBinding.videoTask6, mBinding.videoTask7
        )
        
        // 为每个视频任务的领取按钮设置点击事件
        videoTasks.forEachIndexed { index, taskView ->
            taskView.claimButton.setOnClickListener {
                // 调用ViewModel方法领取视频奖励
                viewModel.claimVideoReward(index)
            }
        }
    }

    /**
     * 设置每日奖励点击监听器
     * 为每个天数按钮设置点击事件
     */
    private fun setupDailyRewardListeners() {
        // 获取所有每日奖励按钮
        val dailyRewards = listOf(
            mBinding.day1Button, mBinding.day2Button, mBinding.day3Button, mBinding.day4Button,
            mBinding.day5Button, mBinding.day6Button, mBinding.day7Button
        )
        
        // 为每个天数按钮设置点击事件
        dailyRewards.forEachIndexed { index, dayButton ->
            dayButton.setOnClickListener {
                // 调用ViewModel方法领取每日奖励
                viewModel.claimDailyReward(index)
            }
        }
    }

    /**
     * 更新视频任务UI
     * 根据任务状态更新每个视频任务的显示样式和按钮状态
     * @param tasks 视频任务列表
     */
    private fun updateVideoTasks(tasks: List<VideoTask>) {
        // 获取所有视频任务视图
        val videoTasks = listOf(
            mBinding.videoTask1, mBinding.videoTask2, mBinding.videoTask3, mBinding.videoTask4,
            mBinding.videoTask5, mBinding.videoTask6, mBinding.videoTask7
        )
        
        // 遍历任务列表，更新对应的UI
        tasks.forEachIndexed { index, task ->
            if (index < videoTasks.size) {
                val taskView = videoTasks[index]
                // 设置奖励时长文本
                taskView.rewardText.text = "+${task.rewardHours}小时"
                
                // 根据任务状态设置不同的UI样式
                when (task.status) {
                    VideoTaskStatus.COMPLETED -> {
                        // 已领取状态：橙色背景，按钮不可点击
                        taskView.claimButton.text = "已领取"
                        taskView.claimButton.isEnabled = false
                        taskView.root.setBackgroundResource(R.drawable.bg_video_task_completed)
                    }
                    VideoTaskStatus.AVAILABLE -> {
                        // 可领取状态：浅橙色背景，按钮可点击
                        taskView.claimButton.text = "去领取"
                        taskView.claimButton.isEnabled = true
                        taskView.root.setBackgroundResource(R.drawable.bg_video_task_available)
                    }
                    VideoTaskStatus.LOCKED -> {
                        // 未解锁状态：灰色背景，按钮不可点击
                        taskView.claimButton.text = "去领取"
                        taskView.claimButton.isEnabled = false
                        taskView.root.setBackgroundResource(R.drawable.bg_video_task_locked)
                    }
                }
            }
        }
    }

    /**
     * 更新每日奖励UI
     * 根据奖励状态更新每个天数按钮的显示样式
     * @param rewards 每日奖励列表
     */
    private fun updateDailyRewards(rewards: List<DailyReward>) {
        // 获取所有每日奖励按钮
        val dailyRewards = listOf(
            mBinding.day1Button, mBinding.day2Button, mBinding.day3Button, mBinding.day4Button,
            mBinding.day5Button, mBinding.day6Button, mBinding.day7Button
        )
        
        // 遍历奖励列表，更新对应的UI
        rewards.forEachIndexed { index, reward ->
            if (index < dailyRewards.size) {
                val dayButton = dailyRewards[index]
                
                // 根据奖励状态设置不同的UI样式
                when (reward.status) {
                    DailyRewardStatus.COMPLETED -> {
                        // 已完成状态：蓝色背景，白色文字
                        dayButton.setBackgroundResource(R.drawable.bg_daily_reward_completed)
                        dayButton.setTextColor(resources.getColor(android.R.color.white, null))
                    }
                    DailyRewardStatus.CURRENT -> {
                        // 当前天状态：蓝色背景，白色文字
                        dayButton.setBackgroundResource(R.drawable.bg_daily_reward_current)
                        dayButton.setTextColor(resources.getColor(android.R.color.white, null))
                    }
                    DailyRewardStatus.LOCKED -> {
                        // 未解锁状态：浅蓝色背景，灰色文字
                        dayButton.setBackgroundResource(R.drawable.bg_daily_reward_locked)
                        dayButton.setTextColor(resources.getColor(R.color.color_8f, null))
                    }
                }
            }
        }
    }
}
