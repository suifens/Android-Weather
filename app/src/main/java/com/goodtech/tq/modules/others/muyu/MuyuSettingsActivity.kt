package com.goodtech.tq.modules.others.muyu

import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.goodtech.tq.R
import com.lxj.xpopup.core.BottomPopupView
import androidx.appcompat.widget.SwitchCompat

class MuyuSettingsActivity(context: Context) : BottomPopupView(context) {

    private lateinit var prefs: SharedPreferences
    private lateinit var vibrationSwitch: SwitchCompat
    private lateinit var soundSwitch: SwitchCompat
    private lateinit var blessingTextSwitch: SwitchCompat
    
    private lateinit var meritCount: TextView
    private lateinit var happinessCount: TextView
    private lateinit var healthCount: TextView
    private lateinit var wealthCount: TextView
    
    private lateinit var meritClear: Button
    private lateinit var happinessClear: Button
    private lateinit var healthClear: Button
    private lateinit var wealthClear: Button
    
    private lateinit var closeButton: ImageView

    override fun getImplLayoutId(): Int = R.layout.dialog_muyu_settings

    override fun onCreate() {
        super.onCreate()
        
        prefs = context.getSharedPreferences("MuyuPrefs", Context.MODE_PRIVATE)
        
        initViews()
        loadSettings()
        setupListeners()
        updateCounts()
    }

    private fun initViews() {
        vibrationSwitch = findViewById(R.id.vibrationSwitch)
        soundSwitch = findViewById(R.id.soundSwitch)
        blessingTextSwitch = findViewById(R.id.blessingTextSwitch)
        
        meritCount = findViewById(R.id.meritCount)
        happinessCount = findViewById(R.id.happinessCount)
        healthCount = findViewById(R.id.healthCount)
        wealthCount = findViewById(R.id.wealthCount)
        
        meritClear = findViewById(R.id.meritClear)
        happinessClear = findViewById(R.id.happinessClear)
        healthClear = findViewById(R.id.healthClear)
        wealthClear = findViewById(R.id.wealthClear)
        
        closeButton = findViewById(R.id.closeButton)
    }

    private fun loadSettings() {
        vibrationSwitch.isChecked = prefs.getBoolean("vibration_enabled", true)
        soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        blessingTextSwitch.isChecked = prefs.getBoolean("blessing_text_enabled", true)
    }

    private fun setupListeners() {
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
        }

        blessingTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("blessing_text_enabled", isChecked).apply()
        }

        meritClear.setOnClickListener {
            clearCount("MERIT")
        }

        happinessClear.setOnClickListener {
            clearCount("HAPPINESS")
        }

        healthClear.setOnClickListener {
            clearCount("HEALTH")
        }

        wealthClear.setOnClickListener {
            clearCount("WEALTH")
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateCounts() {
        meritCount.text = prefs.getInt("count_MERIT", 0).toString()
        happinessCount.text = prefs.getInt("count_HAPPINESS", 0).toString()
        healthCount.text = prefs.getInt("count_HEALTH", 0).toString()
        wealthCount.text = prefs.getInt("count_WEALTH", 0).toString()
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
