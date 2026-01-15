package com.goodtech.tq.modules.others.muyu

import android.content.Context
import com.goodtech.tq.R
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.databinding.DialogMuyuSettingsBinding
import com.lxj.xpopup.core.BottomPopupView

class MuyuSettingsPopup(context: Context) : BottomPopupView(context) {

    private lateinit var binding: DialogMuyuSettingsBinding

    override fun getImplLayoutId(): Int = R.layout.dialog_muyu_settings

    override fun onCreate() {
        super.onCreate()
        binding = DialogMuyuSettingsBinding.bind(popupImplView)
        
        loadSettings()
        setupListeners()
        updateCounts()
    }

    private fun loadSettings() {
        binding.vibrationSwitch.isChecked = SpUtils.getInstance().getBoolean("vibration_enabled", true)
        binding.soundSwitch.isChecked = SpUtils.getInstance().getBoolean("sound_enabled", true)
        binding.blessingTextSwitch.isChecked = SpUtils.getInstance().getBoolean("blessing_text_enabled", true)
    }

    private fun setupListeners() {
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            SpUtils.getInstance().putBoolean("vibration_enabled", isChecked)
        }

        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            SpUtils.getInstance().putBoolean("sound_enabled", isChecked)
        }

        binding.blessingTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            SpUtils.getInstance().putBoolean("blessing_text_enabled", isChecked)
        }

        binding.meritClear.setOnClickListener {
            clearCount("MERIT")
        }

        binding.happinessClear.setOnClickListener {
            clearCount("HAPPINESS")
        }

        binding.healthClear.setOnClickListener {
            clearCount("HEALTH")
        }

        binding.wealthClear.setOnClickListener {
            clearCount("WEALTH")
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateCounts() {
        val meritCount = SpUtils.getInstance().getInt("count_MERIT", 0)
        val happinessCount = SpUtils.getInstance().getInt("count_HAPPINESS", 0)
        val healthCount = SpUtils.getInstance().getInt("count_HEALTH", 0)
        val wealthCount = SpUtils.getInstance().getInt("count_WEALTH", 0)
        
        binding.meritCount.text = meritCount.toString()
        binding.happinessCount.text = happinessCount.toString()
        binding.healthCount.text = healthCount.toString()
        binding.wealthCount.text = wealthCount.toString()
        
        // 根据数值设置清零按钮的可用性和透明度
        updateClearButtonState(binding.meritClear, meritCount)
        updateClearButtonState(binding.happinessClear, happinessCount)
        updateClearButtonState(binding.healthClear, healthCount)
        updateClearButtonState(binding.wealthClear, wealthCount)
    }
    
    private fun updateClearButtonState(button: android.widget.Button, count: Int) {
        if (count == 0) {
            button.isEnabled = false
            button.alpha = 0.4f
        } else {
            button.isEnabled = true
            button.alpha = 1.0f
        }
    }

    private fun clearCount(type: String) {
        SpUtils.getInstance().putInt("count_$type", 0)
        updateCounts()
    }

    override fun onShow() {
        super.onShow()
        updateCounts()
    }
}
