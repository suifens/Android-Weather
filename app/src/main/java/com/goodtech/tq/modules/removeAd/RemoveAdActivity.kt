package com.goodtech.tq.modules.removeAd

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.BarUtils
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTRewardVideoAd
import com.gengee.insaitlib.ui.base.BaseVmActivity
import com.goodtech.tq.R
import com.goodtech.tq.databinding.ActivityRemoveAdBinding
import com.goodtech.tq.modules.removeAd.model.DailyReward
import com.goodtech.tq.modules.removeAd.model.DailyRewardStatus
import com.goodtech.tq.modules.removeAd.model.VideoTask
import com.goodtech.tq.modules.removeAd.model.VideoTaskStatus
import com.goodtech.tq.modules.removeAd.viewmodel.RemoveAdViewModel
import androidx.core.graphics.toColorInt

/**
 * 去广告页面Activity
 * 功能：用户通过观看视频获得去广告时长，包含视频任务和连续签到奖励
 * 架构：使用MVVM模式，ViewBinding进行视图绑定
 */
class RemoveAdActivity : BaseVmActivity<ActivityRemoveAdBinding, RemoveAdViewModel>() {

    companion object {
        private const val TAG = "RemoveAdActivity"
        // 奖励视频广告位ID（如果没有配置，使用默认值，需要根据实际情况修改）
        private const val REWARD_VIDEO_AD_ID = "102948965" // 使用与 DrawDramaActivity 相同的广告位ID
        
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
    
    // 当前正在处理的视频任务索引
    private var currentVideoTaskIndex: Int = -1
    // 当前奖励视频广告
    private var mRewardVideoAd: TTRewardVideoAd? = null
    // 是否已获得奖励
    private var isRewardArrived: Boolean = false

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
        configStationBar(mBinding.topBar)
        // 设置状态栏为浅色模式（黑色文字）
        BarUtils.setStatusBarLightMode(this, true)
        // 设置富文本
        setupRichText()
        
        // 初始化视频任务视图
        setupVideoTasks()
        // 初始化每日奖励视图
        setupDailyRewards()
    }
    
    /**
     * 设置富文本样式
     * 将关键数字和文字设置为橙色（#FF953B）并加粗
     */
    private fun setupRichText() {
        // 设置 video_tasks_desc1 的富文本
        val desc1Text = "每天可以领取7次去广告时长奖励,全部看完\n可获得4天无广告天气预报"
        val desc1Spannable = SpannableString(desc1Text)
        
        // 高亮 "7次" - 橙色 + 粗体
        val index7ci = desc1Text.indexOf("领取7次")
        if (index7ci >= 0) {
            desc1Spannable.setSpan(
                ForegroundColorSpan("#FF953B".toColorInt()),
                index7ci,
                index7ci + 2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            desc1Spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                index7ci,
                index7ci + 2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        // 高亮 "4天无广告" - 橙色 + 粗体
        val index4tian = desc1Text.indexOf("4天无广告")
        if (index4tian >= 0) {
            desc1Spannable.setSpan(
                ForegroundColorSpan("#FF953B".toColorInt()),
                index4tian,
                index4tian + 5,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            desc1Spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                index4tian,
                index4tian + 5,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        mBinding.videoTasksDesc1.text = desc1Spannable
        
        // 设置 daily_rewards_title1 的富文本
        val title1Text = "连续7天看视频领取奖励,必得免费7~100天无广告天气预报"
        val title1Spannable = SpannableString(title1Text)
        
        // 高亮 "连续7天" - 橙色 + 粗体
        val indexLianxu7 = title1Text.indexOf("连续7天")
        if (indexLianxu7 >= 0) {
            title1Spannable.setSpan(
                ForegroundColorSpan("#FF953B".toColorInt()),
                indexLianxu7,
                indexLianxu7 + 4,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            title1Spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                indexLianxu7,
                indexLianxu7 + 4,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        // 高亮 "7~100天无广告" - 橙色 + 粗体
        val index7to100 = title1Text.indexOf("7~100天无广告")
        if (index7to100 >= 0) {
            title1Spannable.setSpan(
                ForegroundColorSpan("#FF953B".toColorInt()),
                index7to100,
                index7to100 + 8,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            title1Spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                index7to100,
                index7to100 + 8,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        mBinding.dailyRewardsTitle1.text = title1Spannable
        
        // 设置 video_tasks_note 的富文本
        val noteText = "第2天可重新领取7次奖励"
        val noteSpannable = SpannableString(noteText)
        
        // 高亮 "7次" - 橙色 + 粗体
        val index7ciNote = noteText.indexOf("7次")
        if (index7ciNote >= 0) {
            noteSpannable.setSpan(
                ForegroundColorSpan("#FF953B".toColorInt()),
                index7ciNote,
                index7ciNote + 2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            noteSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                index7ciNote,
                index7ciNote + 2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        mBinding.videoTasksNote.text = noteSpannable
    }

    /**
     * 初始化事件监听器
     * 设置返回按钮、续时长按钮、视频任务和每日奖励的点击事件
     */
    override fun initEvent() {
        // 返回按钮点击事件
        mBinding.buttonBack.setOnClickListener { finish() }
        
        // 续时长按钮点击事件 - 实现去领取功能
        mBinding.renewButton.setOnClickListener { 
            // 检查是否有可领取的视频任务
            val currentTask = com.goodtech.tq.utils.AdRemovalManager.getCurrentAvailableVideoTask()
            if (currentTask != null) {
                val (taskIndex, rewardHours) = currentTask
                // 播放奖励视频广告
                currentVideoTaskIndex = taskIndex
                loadAndShowRewardVideoAd(rewardHours)
            } else {
                // 没有可领取的任务
                Toast.makeText(
                    this@RemoveAdActivity,
                    "所有视频任务已完成，明天再来吧！",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
        viewModel.remainingDayLiveData.observe(this) { remainingTime ->
            mBinding.remainingDayText.text = remainingTime
        }

        viewModel.remainingHourLiveData.observe(this) { remainingTime ->
            mBinding.remainingHourText.text = remainingTime
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
        // 视频编号会在 updateVideoTasks 中设置，这里不需要单独设置
    }

    /**
     * 设置每日奖励视图
     * 初始化7天连续签到的显示文本
     */
    private fun setupDailyRewards() {
        // 文本会在 updateDailyRewards 中设置
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
            taskView.setOnClaimClickListener {
                // 获取当前任务
                val tasks = viewModel.videoTasksLiveData.value
                if (tasks != null && index < tasks.size && tasks[index].status == VideoTaskStatus.AVAILABLE) {
                    // 播放奖励视频广告
                    currentVideoTaskIndex = index
                    loadAndShowRewardVideoAd(tasks[index].rewardHours)
                }
            }
        }
    }
    
    /**
     * 加载并播放奖励视频广告
     * @param rewardHours 奖励时长（小时）
     */
    private fun loadAndShowRewardVideoAd(rewardHours: Int) {
        isRewardArrived = false
        mRewardVideoAd = null
        
        val adSlot = AdSlot.Builder()
            .setCodeId(REWARD_VIDEO_AD_ID)
            .build()
        
        TTAdSdk.getAdManager().createAdNative(this).loadRewardVideoAd(adSlot, object : TTAdNative.RewardVideoAdListener {
            override fun onError(code: Int, message: String?) {
                Log.e(TAG, "加载奖励视频广告失败: code=$code, message=$message")
                Toast.makeText(this@RemoveAdActivity, "广告加载失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
            
            override fun onRewardVideoAdLoad(ad: TTRewardVideoAd?) {
                ad?.apply {
                    setRewardAdInteractionListener(object : TTRewardVideoAd.RewardAdInteractionListener {
                        override fun onAdShow() {
                            Log.d(TAG, "奖励视频广告展示")
                        }
                        
                        override fun onAdVideoBarClick() {
                            Log.d(TAG, "奖励视频广告点击")
                        }
                        
                        override fun onAdClose() {
                            Log.d(TAG, "奖励视频广告关闭")
                            mRewardVideoAd = null
                        }
                        
                        override fun onVideoComplete() {
                            Log.d(TAG, "奖励视频广告播放完成")
                        }
                        
                        override fun onVideoError() {
                            Log.e(TAG, "奖励视频广告播放出错")
                            Toast.makeText(this@RemoveAdActivity, "广告播放出错", Toast.LENGTH_SHORT).show()
                            mRewardVideoAd = null
                        }
                        
                        override fun onRewardVerify(
                            rewardVerify: Boolean,
                            rewardAmount: Int,
                            rewardName: String,
                            errorCode: Int,
                            errorMsg: String
                        ) {
                            // 已废弃，使用 onRewardArrived
                        }
                        
                        override fun onRewardArrived(isRewardValid: Boolean, rewardType: Int, extraInfo: Bundle) {
                            Log.d(TAG, "奖励到达: isRewardValid=$isRewardValid")
                            isRewardArrived = isRewardValid
                            
                            if (isRewardValid) {
                                // 视频广告完整观看完成，按照 video_tasks_grid 的逻辑增加去广告时长
                                val rewardResult = com.goodtech.tq.utils.AdRemovalManager.claimVideoTaskReward()
                                if (rewardResult != null && rewardResult.first) {
                                    val rewardHours = rewardResult.second
                                    
                                    // 重新加载数据以更新UI（包括每日奖励，因为连续观看天数已更新）
                                    viewModel.loadAdRemovalData()
                                    
                                    // 显示提示信息
                                    val continuousDays = com.goodtech.tq.utils.AdRemovalManager.getContinuousDays()
                                    Toast.makeText(
                                        this@RemoveAdActivity,
                                        "恭喜！获得去广告${rewardHours}小时\n连续观看${continuousDays}天",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    
                                    Log.d(TAG, "视频任务奖励领取成功: 任务奖励${rewardHours}小时，连续观看${continuousDays}天")
                                } else {
                                    Toast.makeText(
                                        this@RemoveAdActivity,
                                        "所有视频任务已完成，明天再来吧！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.w(TAG, "视频任务奖励领取失败或所有任务已完成")
                                }
                            } else {
                                Toast.makeText(this@RemoveAdActivity, "请完整观看视频", Toast.LENGTH_SHORT).show()
                            }
                        }
                        
                        override fun onSkippedVideo() {
                            Log.d(TAG, "用户跳过了视频")
                            if (!isRewardArrived) {
                                Toast.makeText(this@RemoveAdActivity, "请完整观看视频才能获得奖励", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    
                    // 展示广告
                    mRewardVideoAd = this
                    this.showRewardVideoAd(this@RemoveAdActivity)
                }
            }
            
            override fun onRewardVideoCached() {
                Log.d(TAG, "奖励视频广告缓存完成")
            }
            
            override fun onRewardVideoCached(ad: TTRewardVideoAd?) {
                Log.d(TAG, "奖励视频广告缓存完成（带参数）")
            }
        })
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
                // 设置视频编号
                taskView.setVideoNumber(task.taskNumber)
                // 设置奖励时长
                taskView.setRewardHours(task.rewardHours)
                
                // 根据任务状态设置不同的UI样式
                when (task.status) {
                    VideoTaskStatus.COMPLETED -> {
                        // 已领取状态：选中状态（橙色背景）
                        taskView.setState(com.goodtech.tq.modules.removeAd.view.VideoTaskView.TaskState.SELECTED)
                        taskView.setButtonText("已领取")
                        taskView.setButtonEnabled(false)
                    }
                    VideoTaskStatus.AVAILABLE -> {
                        // 可领取状态：当前状态（浅橙色背景）
                        taskView.setState(com.goodtech.tq.modules.removeAd.view.VideoTaskView.TaskState.CURRENT)
                        taskView.setButtonText("去领取")
                        taskView.setButtonEnabled(true)
                    }
                    VideoTaskStatus.LOCKED -> {
                        // 未解锁状态：未来状态（浅橙色背景+灰色边框）
                        taskView.setState(com.goodtech.tq.modules.removeAd.view.VideoTaskView.TaskState.FUTURE)
                        taskView.setButtonText("去领取")
                        taskView.setButtonEnabled(false)
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
                // 设置天数文本
                dayButton.setDayText(reward.dayNumber)

                // 根据奖励状态设置不同的UI样式
                when (reward.status) {
                    DailyRewardStatus.COMPLETED -> {
                        // 已完成状态：选中状态（蓝色背景，白色文字，带对勾图标）
                        dayButton.setSelectedState(true)
                    }
                    DailyRewardStatus.CURRENT -> {
                        // 当前天状态：选中状态（蓝色背景，白色文字，带对勾图标）
                        dayButton.setSelectedState(true)
                    }
                    DailyRewardStatus.LOCKED -> {
                        // 未解锁状态：未选中状态（白色背景，蓝色文字，无图标）
                        dayButton.setSelectedState(false)
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 释放广告资源
        mRewardVideoAd?.getMediationManager()?.destroy()
        mRewardVideoAd = null
    }
}
