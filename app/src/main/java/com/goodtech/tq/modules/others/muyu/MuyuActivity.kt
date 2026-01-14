package com.goodtech.tq.modules.others.muyu

import android.content.SharedPreferences
import com.goodtech.tq.modules.others.muyu.MuyuSettingsActivity
import com.lxj.xpopup.XPopup
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.goodtech.tq.R
import com.goodtech.tq.activity.BaseActivity
import java.util.Random

class MuyuActivity : BaseActivity() {

    private lateinit var muyuImage: ImageView
    private lateinit var blessingContainer: FrameLayout
    private lateinit var countText: TextView
    private lateinit var currentBlessingText: TextView
    private lateinit var currentBlessingIcon: ImageView
    
    private lateinit var blessingMerit: LinearLayout
    private lateinit var blessingHappiness: LinearLayout
    private lateinit var blessingHealth: LinearLayout
    private lateinit var blessingWealth: LinearLayout
    
    private lateinit var settingsButton: ImageView
    
    private var currentBlessingType = BlessingType.MERIT
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: SharedPreferences
    
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
        setContentView(R.layout.activity_muyu)

        prefs = getSharedPreferences("MuyuPrefs", MODE_PRIVATE)
        
        initViews()
        loadSavedData()
        setupClickListeners()
        updateUI()
    }

    private fun initViews() {
        muyuImage = findViewById(R.id.muyuImage)
        blessingContainer = findViewById(R.id.blessingContainer)
        countText = findViewById(R.id.countText)
        currentBlessingText = findViewById(R.id.currentBlessingText)
        currentBlessingIcon = findViewById(R.id.currentBlessingIcon)
        
        blessingMerit = findViewById(R.id.blessingMerit)
        blessingHappiness = findViewById(R.id.blessingHappiness)
        blessingHealth = findViewById(R.id.blessingHealth)
        blessingWealth = findViewById(R.id.blessingWealth)
        
        settingsButton = findViewById(R.id.settingsButton)
    }

    private fun loadSavedData() {
        val savedType = prefs.getInt("currentBlessingType", BlessingType.MERIT.ordinal)
        currentBlessingType = BlessingType.values()[savedType]
    }

    private fun setupClickListeners() {
        // 木鱼点击
        muyuImage.setOnClickListener {
            onMuyuClicked()
        }

        // 祝福类型选择
        blessingMerit.setOnClickListener {
            switchBlessingType(BlessingType.MERIT)
        }
        blessingHappiness.setOnClickListener {
            switchBlessingType(BlessingType.HAPPINESS)
        }
        blessingHealth.setOnClickListener {
            switchBlessingType(BlessingType.HEALTH)
        }
        blessingWealth.setOnClickListener {
            switchBlessingType(BlessingType.WEALTH)
        }

        // 设置按钮
        settingsButton.setOnClickListener {
            val popup = MuyuSettingsActivity(this)
            XPopup.Builder(this)
                .isDestroyOnDismiss(true)
                .asCustom(popup)
                .show()
        }
    }

    private fun switchBlessingType(type: BlessingType) {
        currentBlessingType = type
        prefs.edit().putInt("currentBlessingType", type.ordinal).apply()
        updateUI()
    }

    private fun updateUI() {
        // 更新木鱼图片
        muyuImage.setImageResource(currentBlessingType.muyuRes)
        
        // 更新当前祝福类型显示
        currentBlessingText.text = currentBlessingType.displayName
        currentBlessingIcon.setImageResource(currentBlessingType.iconRes)
        
        // 更新计数
        updateCount()
    }

    private fun updateCount() {
        val count = getCount(currentBlessingType)
        countText.text = count.toString()
    }

    private fun getCount(type: BlessingType): Int {
        return prefs.getInt("count_${type.name}", 0)
    }

    private fun incrementCount(type: BlessingType) {
        val current = getCount(type)
        prefs.edit().putInt("count_${type.name}", current + 1).apply()
        if (type == currentBlessingType) {
            updateCount()
        }
    }

    private fun onMuyuClicked() {
        // 检查设置
        val showBlessingText = prefs.getBoolean("blessing_text_enabled", true)
        val vibrationEnabled = prefs.getBoolean("vibration_enabled", true)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)

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
        muyuImage.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                muyuImage.animate()
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
        muyuImage.getLocationOnScreen(muyuLocation)
        
        // 获取容器的位置
        val containerLocation = IntArray(2)
        blessingContainer.getLocationOnScreen(containerLocation)
        
        // 计算祝福语应该从木鱼顶部开始的位置（相对于容器）
        // muyuLocation[1] 是木鱼在屏幕上的Y坐标
        // containerLocation[1] 是容器在屏幕上的Y坐标
        // muyuImage.height / 2 是木鱼高度的一半，让文字从木鱼顶部开始
        val startY = (muyuLocation[1] - containerLocation[1] - muyuImage.height / 2).toFloat()
        
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

        blessingContainer.addView(blessingText)
        blessingContainer.requestLayout()

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
                        blessingContainer.removeView(blessingText)
                    }
                    .start()
            }
            .start()
    }

    override fun onResume() {
        super.onResume()
        updateCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
