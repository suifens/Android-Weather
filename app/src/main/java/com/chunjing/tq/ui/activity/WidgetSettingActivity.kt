package com.chunjing.tq.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.BarUtils
import com.chunjing.tq.R
import com.chunjing.tq.databinding.ActivityWidgetSettingBinding
import com.chunjing.tq.ui.activity.WidgetSettingActivity.WidgetType.*
import com.chunjing.tq.ui.base.BaseActivity
import com.goodtech.weatherlib.utils.SpUtils

const val WIDGET_TYPE = "WIDGET_TYPE"
const val WIDGET_TRANSPARENT = "WIDGET_TRANSPARENT"
const val WIDGET_DOUBLE = "WIDGET_DOUBLE"

class WidgetSettingActivity : BaseActivity<ActivityWidgetSettingBinding>() {

    private var isSingle = true
    private var isClear = false

    private var hadChanged = false

    override fun bindView() = ActivityWidgetSettingBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {

    }

    override fun initView() {

        configStationBar(mBinding.privateStationBar)
        mBinding.backButton.setOnClickListener { finish() }
        BarUtils.setStatusBarLightMode(this, true)

        initSpannable()
    }

    override fun initEvent() {
        mBinding.singleBtn.setOnClickListener {
            selectedSingle()
            hadChanged = true
        }

        mBinding.doubleBtn.setOnClickListener {
            selectedDouble()
            hadChanged = true
        }

        mBinding.clearSwitch.setOnClickListener {
            isClear = mBinding.clearSwitch.isChecked
            changePreview()
            hadChanged = true
        }
    }

    override fun initData() {

        val widgetTypeStr = SpUtils.instance.getString(WIDGET_TYPE, SingleLine1.toString())
        configType(WidgetType.valueOf(widgetTypeStr))
    }

    override fun onDestroy() {
        if (hadChanged) {
//            val intent = Intent(this, MyWidget::class.java)
//            intent.putExtra("WidgetUpdate", true)
//            sendBroadcast(intent)
//
//            val doubleIntent = Intent(this, MyDoubleWidget::class.java)
//            doubleIntent.putExtra("WidgetUpdate", true)
//            sendBroadcast(doubleIntent)
        }
        super.onDestroy()
    }

    private fun selectedSingle() {
        setup(mBinding.singleBtn, true)
        setup(mBinding.doubleBtn, false)
        isSingle = true
        changePreview()
    }

    private fun selectedDouble() {
        setup(mBinding.doubleBtn, true)
        setup(mBinding.singleBtn, false)
        isSingle = false
        changePreview()
    }

    private fun setup(btn: Button, isSelected: Boolean) {
        if (isSelected) {
            btn.setBackgroundResource(R.drawable.bg_radius_theme)
            btn.setTextColor(Color.WHITE)
        } else {
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.setTextColor(Color.parseColor("#8B98E4"))
        }
    }

    private fun changePreview() {
        val type: WidgetType = if (isSingle) {
            if (isClear) SingleLine2 else SingleLine1
        } else {
            if (isClear) DoubleLine2 else DoubleLine1
        }
        changePreview(type)
    }

    private fun changePreview(type: WidgetType) {
        SpUtils.instance.putString(WIDGET_TYPE, type.toString())
        when (type) {
            SingleLine1 -> mBinding.previewImgView.setImageResource(R.drawable.img_widget_mb)
            SingleLine2 -> mBinding.previewImgView.setImageResource(R.drawable.img_widget_mc)
            DoubleLine1 -> mBinding.previewImgView.setImageResource(R.drawable.img_widget_bb)
            DoubleLine2 -> mBinding.previewImgView.setImageResource(R.drawable.img_widget_bc)
        }
    }

    private fun configType(type: WidgetType) {
        when (type) {
            SingleLine1 -> {
                selectedSingle()
                mBinding.clearSwitch.isChecked = false
            }
            SingleLine2 -> {
                selectedSingle()
                mBinding.clearSwitch.isChecked = true
            }
            DoubleLine1 -> {
                selectedDouble()
                mBinding.clearSwitch.isChecked = false
            }
            DoubleLine2 -> {
                selectedDouble()
                mBinding.clearSwitch.isChecked = true
            }
        }
        changePreview(type)
    }

    /// 配置 spannable
    private fun initSpannable() {
        mBinding.tipTextView.let {
            val permissionStr = resources.getString(R.string.widget_tip)
            val spannableString = SpannableString(permissionStr)

            val boldColor1 = resources.getString(R.string.widget_tip_bold_color1)
            val boldColor1Start: Int = permissionStr.indexOf(boldColor1)
            val boldColor1End: Int = boldColor1Start + boldColor1.length
            val colorSpan1 = ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_theme))

            val boldColor2 = resources.getString(R.string.widget_tip_bold_color2)
            val boldColor2Start: Int = permissionStr.indexOf(boldColor2)
            val boldColor2End: Int = boldColor2Start + boldColor2.length
            val colorSpan2 = ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_theme))

            val boldColor3 = resources.getString(R.string.widget_tip_bold_color3)
            val boldColor3Start: Int = permissionStr.indexOf(boldColor3)
            val boldColor3End: Int = boldColor3Start + boldColor3.length
            val colorSpan3 = ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_theme))

            val boldColor4 = resources.getString(R.string.widget_tip_bold_color4)
            val boldColor4Start: Int = permissionStr.indexOf(boldColor4)
            val boldColor4End: Int = boldColor4Start + boldColor4.length
            val colorSpan4 = ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_theme))

            spannableString.setSpan(colorSpan1, boldColor1Start, boldColor1End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(colorSpan2, boldColor2Start, boldColor2End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(colorSpan3, boldColor3Start, boldColor3End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(colorSpan4, boldColor4Start, boldColor4End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), boldColor1Start, boldColor1End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), boldColor2Start, boldColor2End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), boldColor3Start, boldColor3End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), boldColor4Start, boldColor4End, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            //粗体
            val boldStr = resources.getString(R.string.widget_tip_bold)
            val boldStart: Int = permissionStr.indexOf(boldStr)
            val boldEnd: Int = boldStart + boldStr.length
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                boldStart,
                boldEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )

            it.movementMethod = LinkMovementMethod.getInstance()
            it.text = spannableString
        }
    }

    public enum class WidgetType {
        SingleLine1, SingleLine2, DoubleLine1, DoubleLine2
    }

}