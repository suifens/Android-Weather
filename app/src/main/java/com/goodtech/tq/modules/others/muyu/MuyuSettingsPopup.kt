package com.goodtech.tq.modules.others.muyu

import android.content.Context
import android.content.SharedPreferences
import com.goodtech.tq.R
import com.goodtech.tq.databinding.DialogMuyuSettingsBinding
import com.lxj.xpopup.core.BottomPopupView

class MuyuSettingsPopup(context: Context) : BottomPopupView(context) {

    private lateinit var binding: DialogMuyuSettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun getImplLayoutId(): Int = R.layout.dialog_muyu_settings

    override fun onCreate() {
        super.onCreate()
        binding = DialogMuyuSettingsBinding.bind(popupImplView)
        
        prefs = context.getSharedPreferences("MuyuPrefs", Context.MODE_PRIVATE)
        
        loadSettings()
        setupListeners()
        updateCounts()
    }

    private fun loadSettings() {
        binding.vibrationSwitch.isChecked = prefs.getBoolean("vibration_enabled", true)
        binding.soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        binding.blessingTextSwitch.isChecked = prefs.getBoolean("blessing_text_enabled", true)
    }

    private fun setupListeners() {
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
        }

        binding.blessingTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("blessing_text_enabled", isChecked).apply()
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
        val meritCount = prefs.getInt("count_MERIT", 0)
        val happinessCount = prefs.getInt("count_HAPPINESS", 0)
        val healthCount = prefs.getInt("count_HEALTH", 0)
        val wealthCount = prefs.getInt("count_WEALTH", 0)
        
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
        prefs.edit().putInt("count_$type", 0).apply()
        updateCounts()
    }

    override fun onShow() {
        super.onShow()
        updateCounts()
    }
}
