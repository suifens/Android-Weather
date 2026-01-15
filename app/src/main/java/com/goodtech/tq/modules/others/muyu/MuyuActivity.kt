package com.goodtech.tq.modules.others.muyu

import com.goodtech.tq.utils.SpUtils
import com.lxj.xpopup.XPopup
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
    
    // 祝福类型枚举
    enum class BlessingType(val displayName: String, val iconRes: Int, val muyuRes: Int, val smallIconRes: Int) {
        MERIT("功德圆满", R.drawable.ic_merit, R.drawable.img_merit, R.drawable.img_merit),
        HAPPINESS("幸福安康", R.drawable.ic_happiness, R.drawable.img_happiness, R.drawable.img_happiness),
        HEALTH("无病无灾", R.drawable.ic_health, R.drawable.img_health, R.drawable.img_health),
        WEALTH("暴美暴富", R.drawable.ic_wealth, R.drawable.img_wealth, R.drawable.img_wealth)
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
        updateUI()
        loadAd()
    }

    private fun loadSavedData() {
        val savedType = SpUtils.getInstance().getInt("currentBlessingType", BlessingType.MERIT.ordinal)
        currentBlessingType = BlessingType.values()[savedType]
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
        // 更新木鱼图片
        binding.muyuImage.setImageResource(currentBlessingType.muyuRes)
        
        // 更新当前祝福类型显示
        binding.currentBlessingText.text = currentBlessingType.displayName
        binding.currentBlessingIcon.setImageResource(currentBlessingType.iconRes)
        
        // 更新选中状态的图片缩放
        updateBlessingIconsScale()
        
        // 更新计数
        updateCount()
    }

    private fun loadAd() {
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
        // 重置所有图标为正常大小
        binding.blessingMeritIcon.scaleX = 1.0f
        binding.blessingMeritIcon.scaleY = 1.0f
        binding.blessingHappinessIcon.scaleX = 1.0f
        binding.blessingHappinessIcon.scaleY = 1.0f
        binding.blessingHealthIcon.scaleX = 1.0f
        binding.blessingHealthIcon.scaleY = 1.0f
        binding.blessingWealthIcon.scaleX = 1.0f
        binding.blessingWealthIcon.scaleY = 1.0f
        
        // 将当前选中的图标放大到1.3倍（带动画效果）
        when (currentBlessingType) {
            BlessingType.MERIT -> {
                binding.blessingMeritIcon.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .start()
            }
            BlessingType.HAPPINESS -> {
                binding.blessingHappinessIcon.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .start()
            }
            BlessingType.HEALTH -> {
                binding.blessingHealthIcon.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .start()
            }
            BlessingType.WEALTH -> {
                binding.blessingWealthIcon.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .start()
            }
        }
    }

    private fun updateCount() {
        val count = getCount(currentBlessingType)
        binding.countText.text = count.toString()
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

        // 音效（如果有）
        if (soundEnabled) {
            // 可以在这里添加音效播放
        }

        // 木鱼点击动画
        animateMuyuClick()

        // 显示祝福语
        if (showBlessingText) {
            showBlessing()
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

    private fun animateMuyuClick() {
        binding.muyuImage.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                binding.muyuImage.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun showBlessing() {
        val blessings = blessingTexts[currentBlessingType] ?: return
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
            setTextColor(getColor(R.color.black))
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
                // 然后向上移动并逐渐淡出，保持水平偏移
                blessingText.animate()
                    .alpha(0f)
                    .translationY(endY)
                    .setDuration(1500)
                    .setInterpolator(LinearInterpolator())
                    .withEndAction {
                        binding.blessingContainer.removeView(blessingText)
                    }
                    .start()
            }
            .start()
    }

    override fun onResume() {
        super.onResume()
        updateCount()
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
        super.onDestroy()
        mBannerAd?.let {
            try {
                it.expressAdView?.let { adView ->
                    (adView.parent as? ViewGroup)?.removeView(adView)
                }
                it.destroy()
            } catch (e: Exception) {
                Log.e("TAG", "removeAdView error: ${e.message}")
            }
        }
        handler.removeCallbacksAndMessages(null)
    }
}
