package com.chunjing.tq.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.goodtech.weatherlib.skin.SkinViewSupport
import com.goodtech.weatherlib.utils.WeatherUtils

class PluginImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), SkinViewSupport {

    private var iconCode: Int = 0

    override fun applySkin() {
        setImageDrawable(WeatherUtils.getTempIcon(context, iconCode))
    }

    fun setImageResourceName(iconDay: Int) {
        iconCode = iconDay
        setImageDrawable(WeatherUtils.getTempIcon(context, iconDay))
    }
}