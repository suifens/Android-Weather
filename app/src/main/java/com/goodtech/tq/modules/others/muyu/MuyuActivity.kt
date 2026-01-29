package com.goodtech.tq.modules.others.muyu

import com.goodtech.tq.utils.SpUtils
import com.lxj.xpopup.XPopup
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bytedance.sdk.openadsdk.TTAdDislike.DislikeInteractionCallback
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.gengee.insaitlib.ext.clickNoRepeat
import com.goodtech.tq.BuildConfig
import com.goodtech.tq.R
import com.goodtech.tq.activity.BaseActivity
import com.goodtech.tq.ad.AdManager
import com.goodtech.tq.app.App.Companion.instance
import com.goodtech.tq.base.callback.DataCallback
import com.goodtech.tq.databinding.ActivityMuyuBinding
import java.util.Random

class MuyuActivity : BaseActivity() {

    private lateinit var binding: ActivityMuyuBinding
    private var mBannerAd: TTNativeExpressAd? = null
    private var currentBlessingType = BlessingType.MERIT
    private val handler = Handler(Looper.getMainLooper())
    
    // 音效播放器
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isSoundLoaded: Boolean = false
    private var isDestroyed: Boolean = false
    
    // 祝福类型枚举
    enum class BlessingType(val displayName: String, val iconRes: Int, val muyuRes: Int, val bgRes: Int) {
        MERIT("功德圆满", R.drawable.ic_merit, R.drawable.img_merit, R.drawable.bg_gradient_merit),
        HAPPINESS("幸福安康", R.drawable.ic_happiness, R.drawable.img_happiness, R.drawable.bg_gradient_happiness),
        HEALTH("无病无灾", R.drawable.ic_health, R.drawable.img_health, R.drawable.bg_gradient_health),
        WEALTH("暴美暴富", R.drawable.ic_wealth, R.drawable.img_wealth, R.drawable.bg_gradient_wealth)
    }
    
    // 祝福语列表
    private val blessingTexts = mapOf(
        BlessingType.MERIT to listOf("功德+1", "功德圆满", "善行+1", "福报+1"),
        BlessingType.HAPPINESS to listOf("幸福+1", "幸福安康", "快乐+1", "安康+1"),
        BlessingType.HEALTH to listOf("健康+1", "无病无灾", "身体+1", "平安+1"),
        BlessingType.WEALTH to listOf("暴美+1", "暴美暴富", "财富+1", "暴富+1")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMuyuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configStationBar(binding.headerBar);
        
        // 确保 clipChildren 和 clipToPadding 在代码层面生效
        binding.root.clipChildren = false
        binding.root.clipToPadding = false
        binding.blessingTypeBar.clipChildren = false
        binding.blessingTypeBar.clipToPadding = false
        binding.blessingMerit.clipChildren = false
        binding.blessingMerit.clipToPadding = false
        binding.blessingHappiness.clipChildren = false
        binding.blessingHappiness.clipToPadding = false
        binding.blessingHealth.clipChildren = false
        binding.blessingHealth.clipToPadding = false
        binding.blessingWealth.clipChildren = false
        binding.blessingWealth.clipToPadding = false
        
        loadSavedData()
        setupClickListeners()
        initSoundPool()
        updateUI()
        loadAd()
    }
    
    /**
     * 初始化音效播放器
     */
    private fun initSoundPool() {
        if (isDestroyed) return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                soundPool = SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                soundPool = SoundPool(1, android.media.AudioManager.STREAM_MUSIC, 0)
            }
            
            if (soundPool == null) {
                Log.w("MuyuActivity", "SoundPool 创建失败")
                return
            }
            
            // 加载音效文件
            // 优先从 res/raw 加载，如果不存在则从 assets 加载
            var assetFileDescriptor: android.content.res.AssetFileDescriptor? = null
            
            try {
                // 方式1：尝试从 res/raw 加载（推荐，编译时打包）
                val resourceId = resources.getIdentifier("muyu_sound", "raw", packageName)
                if (resourceId != 0) {
                    soundId = soundPool?.load(this, resourceId, 1) ?: 0
                    if (soundId > 0) {
                        Log.d("MuyuActivity", "音效文件从 res/raw 加载成功, soundId: $soundId")
                        isSoundLoaded = true
                    }
                }
            } catch (e: android.content.res.Resources.NotFoundException) {
                Log.w("MuyuActivity", "res/raw 中未找到音效文件: ${e.message}")
            } catch (e: Exception) {
                Log.w("MuyuActivity", "从 res/raw 加载音效失败: ${e.message}")
            }
            
            // 方式2：如果 res/raw 中没有，尝试从 assets 加载
            if (!isSoundLoaded) {
                try {
                    assetFileDescriptor = assets.openFd("muyu_sound.mp3")
                    soundId = soundPool?.load(assetFileDescriptor, 1) ?: 0
                    if (soundId > 0) {
                        Log.d("MuyuActivity", "音效文件从 assets 加载成功, soundId: $soundId")
                        isSoundLoaded = true
                    }
                } catch (e: java.io.FileNotFoundException) {
                    Log.w("MuyuActivity", "assets 中未找到音效文件: ${e.message}")
                } catch (e: Exception) {
                    Log.w("MuyuActivity", "从 assets 加载音效失败: ${e.message}")
                } finally {
                    try {
                        assetFileDescriptor?.close()
                    } catch (e: Exception) {
                        Log.w("MuyuActivity", "关闭 AssetFileDescriptor 失败: ${e.message}")
                    }
                }
            }
            
            if (!isSoundLoaded) {
                Log.w("MuyuActivity", "未找到音效文件，将跳过音效播放。请将 muyu_sound.mp3 放到 res/raw 或 assets 文件夹中")
                soundId = 0
            }
        } catch (e: Exception) {
            Log.e("MuyuActivity", "初始化音效播放器失败: ${e.message}", e)
            soundId = 0
            isSoundLoaded = false
            // 确保资源被清理
            try {
                soundPool?.release()
            } catch (e2: Exception) {
                Log.e("MuyuActivity", "清理 SoundPool 失败: ${e2.message}")
            }
            soundPool = null
        }
    }

    private fun loadSavedData() {
        try {
            val savedType = SpUtils.getInstance().getInt("currentBlessingType", BlessingType.MERIT.ordinal)
            val types = BlessingType.values()
            if (savedType in types.indices) {
                currentBlessingType = types[savedType]
            } else {
                currentBlessingType = BlessingType.MERIT
            }
        } catch (e: Exception) {
            Log.e("MuyuActivity", "加载保存数据失败: ${e.message}", e)
            currentBlessingType = BlessingType.MERIT
        }
    }

    private fun setupClickListeners() {

        binding.backButton.clickNoRepeat { finish() }

        // 木鱼点击
        binding.muyuImage.setOnClickListener {
            onMuyuClicked()
        }

        // 祝福类型选择
        binding.blessingMerit.setOnClickListener {
            switchBlessingType(BlessingType.MERIT)
        }
        binding.blessingHappiness.setOnClickListener {
            switchBlessingType(BlessingType.HAPPINESS)
        }
        binding.blessingHealth.setOnClickListener {
            switchBlessingType(BlessingType.HEALTH)
        }
        binding.blessingWealth.setOnClickListener {
            switchBlessingType(BlessingType.WEALTH)
        }

        // 设置按钮
        binding.settingsButton.setOnClickListener {
            val popup = MuyuSettingsPopup(this)
            XPopup.Builder(this)
                .isDestroyOnDismiss(true)
                .asCustom(popup)
                .show()
        }
    }

    private fun switchBlessingType(type: BlessingType) {
        currentBlessingType = type
        SpUtils.getInstance().putInt("currentBlessingType", type.ordinal)
        updateUI()
    }

    private fun updateUI() {
        if (isDestroyed || !::binding.isInitialized) return
        
        try {
            // 更新木鱼图片
            binding.muyuImage.setImageResource(currentBlessingType.muyuRes)
            
            // 更新当前祝福类型显示
            binding.currentBlessingText.text = currentBlessingType.displayName
            binding.currentBlessingIcon.setImageResource(currentBlessingType.iconRes)

            binding.containerLayout.setBackgroundResource(currentBlessingType.bgRes)
            
            // 更新选中状态的图片缩放
            updateBlessingIconsScale()
            
            // 更新计数
            updateCount()
        } catch (e: Exception) {
            Log.e("MuyuActivity", "更新UI失败: ${e.message}", e)
        }
    }

    private fun loadAd() {
        // 检查是否在去广告有效期内
        if (com.goodtech.tq.utils.AdRemovalManager.isAdRemovalActive()) {
            // 在去广告有效期内，不加载广告
            binding.adContainer.visibility = View.GONE
            binding.adContainer.layoutParams.height = 0
            return
        }
        
        lifecycleScope.launch {
            try {
                val ad = loadExpressAdAsync(
                    BuildConfig.PGE_HOME_BANNER_POS_ID,
                    SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat()),
                    0
                )
                
                // 在主线程更新 UI
                withContext(Dispatchers.Main) {
                    if (ad != null) {
                        mBannerAd = ad
                        binding.adContainer.visibility = View.VISIBLE
                        binding.adContainer.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        showExpressAd(binding.adContainer, ad)
                    } else {
                        binding.adContainer.visibility = View.GONE
                        binding.adContainer.layoutParams.height = 0
                    }
                }
            } catch (e: Exception) {
                Log.e("MuyuActivity", "loadAd error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.adContainer.visibility = View.GONE
                    binding.adContainer.layoutParams.height = 0
                }
            }
        }
    }
    
    private suspend fun loadExpressAdAsync(
        codeId: String,
        width: Int,
        height: Int
    ): TTNativeExpressAd? = suspendCancellableCoroutine { continuation ->
        AdManager.getInstance().loadExpressAd(
            this,
            codeId,
            width,
            height,
            object : DataCallback<TTNativeExpressAd> {
                override fun onComplete(data: TTNativeExpressAd?, errorMsg: String?) {
                    if (continuation.isActive) {
                        if (data != null) {
                            continuation.resume(data)
                        } else {
                            Log.e("MuyuActivity", "loadExpressAd error: $errorMsg")
                            continuation.resume(null)
                        }
                    }
                }
            }
        )
        
        // 如果协程被取消，可以在这里处理
        continuation.invokeOnCancellation {
            // 可以在这里取消广告加载（如果 AdManager 支持）
        }
    }
    
    private fun updateBlessingIconsScale() {
        if (isDestroyed || !::binding.isInitialized) return
        
        try {
            // 重置所有图标为正常大小
            binding.blessingMeritIcon.scaleX = 1.0f
            binding.blessingMeritIcon.scaleY = 1.0f
            binding.blessingHappinessIcon.scaleX = 1.0f
            binding.blessingHappinessIcon.scaleY = 1.0f
            binding.blessingHealthIcon.scaleX = 1.0f
            binding.blessingHealthIcon.scaleY = 1.0f
            binding.blessingWealthIcon.scaleX = 1.0f
            binding.blessingWealthIcon.scaleY = 1.0f
            
            // 将当前选中的图标放大到1.5倍（带动画效果）
            when (currentBlessingType) {
                BlessingType.MERIT -> {
                    binding.blessingMeritIcon.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(200)
                        .start()
                }
                BlessingType.HAPPINESS -> {
                    binding.blessingHappinessIcon.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(200)
                        .start()
                }
                BlessingType.HEALTH -> {
                    binding.blessingHealthIcon.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(200)
                        .start()
                }
                BlessingType.WEALTH -> {
                    binding.blessingWealthIcon.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(200)
                        .start()
                }
            }
        } catch (e: Exception) {
            Log.e("MuyuActivity", "更新图标缩放失败: ${e.message}", e)
        }
    }

    private fun updateCount() {
        if (isDestroyed || !::binding.isInitialized) return
        
        try {
            val count = getCount(currentBlessingType)
            binding.countText.text = count.toString()
        } catch (e: Exception) {
            Log.e("MuyuActivity", "更新计数失败: ${e.message}", e)
        }
    }

    private fun getCount(type: BlessingType): Int {
        return SpUtils.getInstance().getInt("count_${type.name}", 0)
    }

    private fun incrementCount(type: BlessingType) {
        val current = getCount(type)
        SpUtils.getInstance().putInt("count_${type.name}", current + 1)
        if (type == currentBlessingType) {
            updateCount()
        }
    }

    private fun onMuyuClicked() {
        if (isDestroyed || !::binding.isInitialized) return
        
        try {
            // 检查设置
            val showBlessingText = SpUtils.getInstance().getBoolean("blessing_text_enabled", true)
            val vibrationEnabled = SpUtils.getInstance().getBoolean("vibration_enabled", true)
            val soundEnabled = SpUtils.getInstance().getBoolean("sound_enabled", true)

            // 增加计数
            incrementCount(currentBlessingType)

            // 震动反馈
            if (vibrationEnabled) {
                playVibration()
            }

            // 音效播放
            if (soundEnabled) {
                playSound()
            }

            // 木鱼点击动画
            animateMuyuClick()

            // 显示祝福语
            if (showBlessingText) {
                showBlessing()
            }
        } catch (e: Exception) {
            Log.e("MuyuActivity", "木鱼点击处理失败: ${e.message}", e)
        }
    }

    private fun playVibration() {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 播放木鱼点击音效
     */
    private fun playSound() {
        if (isDestroyed) return
        
        try {
            val pool = soundPool
            if (pool != null && isSoundLoaded && soundId > 0) {
                // 播放音效
                // 参数：soundId, leftVolume, rightVolume, priority, loop, rate
                // leftVolume/rightVolume: 0.0-1.0 音量
                // priority: 优先级，0 最低
                // loop: 循环次数，0 不循环，-1 无限循环
                // rate: 播放速率，1.0 正常速度
                val result = pool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
                if (result == 0) {
                    Log.w("MuyuActivity", "音效播放失败，soundId: $soundId")
                }
            }
        } catch (e: IllegalStateException) {
            // SoundPool 可能已被释放
            Log.w("MuyuActivity", "SoundPool 状态异常: ${e.message}")
            isSoundLoaded = false
        } catch (e: Exception) {
            Log.e("MuyuActivity", "播放音效失败: ${e.message}", e)
        }
    }

    private fun animateMuyuClick() {
        if (isDestroyed || !::binding.isInitialized) return
        
        try {
            binding.muyuImage.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    if (!isDestroyed && ::binding.isInitialized) {
                        try {
                            binding.muyuImage.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start()
                        } catch (e: Exception) {
                            Log.e("MuyuActivity", "动画恢复失败: ${e.message}", e)
                        }
                    }
                }
                .start()
        } catch (e: Exception) {
            Log.e("MuyuActivity", "动画播放失败: ${e.message}", e)
        }
    }

    private fun showBlessing() {
        if (isDestroyed || !::binding.isInitialized) return
        
        try {
            val blessings = blessingTexts[currentBlessingType] ?: return
            if (blessings.isEmpty()) return
            
            val random = Random()
            val blessing = blessings[random.nextInt(blessings.size)]

            // 获取木鱼图片在屏幕上的位置
            val muyuLocation = IntArray(2)
            binding.muyuImage.getLocationOnScreen(muyuLocation)
            
            // 获取容器的位置
            val containerLocation = IntArray(2)
            binding.blessingContainer.getLocationOnScreen(containerLocation)
            
            // 计算祝福语应该从木鱼顶部开始的位置（相对于容器）
            // muyuLocation[1] 是木鱼在屏幕上的Y坐标
            // containerLocation[1] 是容器在屏幕上的Y坐标
            // muyuImage.height / 2 是木鱼高度的一半，让文字从木鱼顶部开始
            val startY = (muyuLocation[1] - containerLocation[1] - binding.muyuImage.height / 2).toFloat()
            
            // 随机水平偏移，让多个祝福语错开显示（-80到+80像素之间）
            val horizontalOffset = (random.nextFloat() - 0.5f) * 160f
            
            // 创建布局参数，水平居中
            val tempLayoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            val blessingText = TextView(this).apply {
                text = blessing
                textSize = 28f
                try {
                    setTextColor(getColor(R.color.black))
                } catch (e: Exception) {
                    setTextColor(android.graphics.Color.BLACK)
                }
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                layoutParams = tempLayoutParams
                alpha = 0f
                visibility = View.VISIBLE
                // 设置初始位置在木鱼顶部，并添加水平偏移
                translationY = startY
                translationX = horizontalOffset
            }

            binding.blessingContainer.addView(blessingText)
            binding.blessingContainer.requestLayout()

            // 从木鱼顶部开始，一直向上移动并渐变消失
            // 使用 ObjectAnimator 来同时控制 alpha 和 translationY
            val endY = startY - 500f  // 向上移动500像素
            
            // 先快速淡入
            blessingText.animate()
                .alpha(1f)
                .setDuration(200)
                .withEndAction {
                    if (!isDestroyed && ::binding.isInitialized) {
                        try {
                            // 然后向上移动并逐渐淡出，保持水平偏移
                            blessingText.animate()
                                .alpha(0f)
                                .translationY(endY)
                                .setDuration(1500)
                                .setInterpolator(LinearInterpolator())
                                .withEndAction {
                                    if (!isDestroyed && ::binding.isInitialized) {
                                        try {
                                            binding.blessingContainer.removeView(blessingText)
                                        } catch (e: Exception) {
                                            Log.e("MuyuActivity", "移除祝福语视图失败: ${e.message}", e)
                                        }
                                    }
                                }
                                .start()
                        } catch (e: Exception) {
                            Log.e("MuyuActivity", "祝福语动画失败: ${e.message}", e)
                            try {
                                binding.blessingContainer.removeView(blessingText)
                            } catch (e2: Exception) {
                                Log.e("MuyuActivity", "清理祝福语视图失败: ${e2.message}", e2)
                            }
                        }
                    }
                }
                .start()
        } catch (e: Exception) {
            Log.e("MuyuActivity", "显示祝福语失败: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isDestroyed) {
            updateCount()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停时可以停止音效播放（如果需要）
    }

    private fun showExpressAd(container: FrameLayout, ad: TTNativeExpressAd) {
        container.visibility = View.VISIBLE
        container.background = container.context.getDrawable(R.drawable.bg_round_8)
        container.removeAllViews()
        bindDislike(ad, container)
        ad.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: View?, type: Int) {
                // 广告点击回调
            }

            override fun onAdShow(view: View?, type: Int) {
                // 广告展示回调
            }

            override fun onRenderFail(view: View?, msg: String?, code: Int) {
                // 渲染失败回调
                container.visibility = View.GONE
                container.layoutParams.height = 0
            }

            override fun onRenderSuccess(view: View?, width: Float, height: Float) {
                // 渲染成功回调
                ad.expressAdView?.let { adView ->
                    // 如果广告视图已经有父视图，先移除
                    (adView.parent as? ViewGroup)?.removeView(adView)
                    container.removeAllViews()
                    container.addView(adView)
                }
            }
        })
        ad.render()
    }

    private fun bindDislike(ad: TTNativeExpressAd, container: FrameLayout) {

        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback(instance.mainActivity, object : DislikeInteractionCallback {
            override fun onShow() {
            }

            override fun onSelected(position: Int, value: String, enforce: Boolean) {
                container.removeAllViews()
                mBannerAd = null
            }

            override fun onCancel() {

            }
        })
    }

    override fun onDestroy() {
        isDestroyed = true
        
        // 清理广告
        mBannerAd?.let {
            try {
                it.expressAdView?.let { adView ->
                    (adView.parent as? ViewGroup)?.removeView(adView)
                }
                it.destroy()
            } catch (e: Exception) {
                Log.e("MuyuActivity", "清理广告失败: ${e.message}", e)
            }
        }
        mBannerAd = null
        
        // 释放音效资源
        try {
            soundPool?.release()
        } catch (e: Exception) {
            Log.e("MuyuActivity", "释放 SoundPool 失败: ${e.message}", e)
        }
        soundPool = null
        soundId = 0
        isSoundLoaded = false
        
        // 清理 Handler
        try {
            handler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            Log.e("MuyuActivity", "清理 Handler 失败: ${e.message}", e)
        }
        
        super.onDestroy()
    }
}
