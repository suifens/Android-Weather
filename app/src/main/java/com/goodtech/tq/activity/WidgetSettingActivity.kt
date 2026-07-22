package com.goodtech.tq.activity

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.goodtech.tq.R
import com.goodtech.tq.modules.others.widget.WidgetType
import com.goodtech.tq.utils.Constants
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.widget.WidgetWorkScheduler

/**
 * 桌面小部件样式设置页。
 *
 * 提供单行/双行 + 透明/非透明四种样式选择，选择结果保存到 [Constants.WIDGET_TYPE]。
 */
class WidgetSettingActivity : BaseActivity() {

    private lateinit var clearBtn: ToggleButton
    private lateinit var singleBtn: Button
    private lateinit var doubleBtn: Button
    private lateinit var previewImgView: ImageView

    private var isSingle: Boolean = true
    private var isClear: Boolean = false

    private var hadChanged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_widget)

        findViewById<TextView>(R.id.tv_title)?.text = getString(R.string.title_setting_widget)

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar))

        findViewById<View>(R.id.button_back).setOnClickListener { finish() }

        singleBtn = findViewById(R.id.singleBtn)
        singleBtn.setOnClickListener {
            selectedSingle()
            hadChanged = true
        }

        doubleBtn = findViewById(R.id.doubleBtn)
        doubleBtn.setOnClickListener {
            selectedDouble()
            hadChanged = true
        }

        clearBtn = findViewById(R.id.switchBtn_clear)
        clearBtn.setOnClickListener {
            isClear = clearBtn.isChecked
            changePreview()
            hadChanged = true
        }

        previewImgView = findViewById(R.id.previewImgView)

        configSpannable()

        val widgetTypeStr = SpUtils.getInstance()
            .getString(Constants.WIDGET_TYPE, WidgetType.SingleLine1.toString())
        configType(WidgetType.valueOf(widgetTypeStr))
    }

    override fun onDestroy() {
        if (hadChanged) {
            // 样式变更后立即触发一次刷新，让已安装的 widget 应用新样式
            WidgetWorkScheduler.enqueueOneTime(this)
        }
        super.onDestroy()
    }

    private fun selectedSingle() {
        setup(singleBtn, true)
        setup(doubleBtn, false)
        isSingle = true
        changePreview()
    }

    private fun selectedDouble() {
        setup(doubleBtn, true)
        setup(singleBtn, false)
        isSingle = false
        changePreview()
    }

    private fun setup(btn: Button, isSelected: Boolean) {
        if (isSelected) {
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.setTextColor(Color.WHITE)
        } else {
            btn.setBackgroundColor(Color.WHITE)
            btn.setTextColor(Color.parseColor("#5A9EF2"))
        }
    }

    private fun changePreview() {
        val type: WidgetType = if (isSingle) {
            if (isClear) WidgetType.SingleLine2 else WidgetType.SingleLine1
        } else {
            if (isClear) WidgetType.DoubleLine2 else WidgetType.DoubleLine1
        }
        changePreview(type)
    }

    private fun changePreview(type: WidgetType) {
        SpUtils.getInstance().putString(Constants.WIDGET_TYPE, type.toString())
        when (type) {
            WidgetType.SingleLine1 -> previewImgView.setImageResource(R.drawable.img_preview_1)
            WidgetType.SingleLine2 -> previewImgView.setImageResource(R.drawable.img_preview_2)
            WidgetType.DoubleLine1 -> previewImgView.setImageResource(R.drawable.img_preview_3)
            WidgetType.DoubleLine2 -> previewImgView.setImageResource(R.drawable.img_preview_4)
        }
    }

    private fun configType(type: WidgetType) {
        when (type) {
            WidgetType.SingleLine1 -> {
                selectedSingle()
                clearBtn.isChecked = false
            }
            WidgetType.SingleLine2 -> {
                selectedSingle()
                clearBtn.isChecked = true
            }
            WidgetType.DoubleLine1 -> {
                selectedDouble()
                clearBtn.isChecked = false
            }
            WidgetType.DoubleLine2 -> {
                selectedDouble()
                clearBtn.isChecked = true
            }
        }
        changePreview(type)
    }

    private fun configSpannable() {
        val tipString = ("1.长按“桌面空白处”添加天气小插件，调节不同样式\n" +
                "2.选择“小部件”或“添加插件”\n" +
                "3.找到“天气预报”小插件，长按拖动至桌面\n" +
                "\n" +
                "*oppo手机需要在桌面“两指捏合”添加小工具/小部件\n" +
                "*为保证插件及通知功能的正常使用，请在系统中设置\n" +
                "为允许自动启动(开机启动或后台运行)")
        val spannableString = SpannableString(tipString)
        val blueString = "“桌面空白处”"
        val agreementStart = tipString.indexOf(blueString)
        val agreementEnd = agreementStart + blueString.length
        val agreementColorSp = ForegroundColorSpan(Color.parseColor("#5A9EF2"))
        spannableString.setSpan(
            agreementColorSp, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        findViewById<TextView>(R.id.tipTextView).text = spannableString
    }
}
