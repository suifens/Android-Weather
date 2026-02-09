package com.goodtech.tq.views.popup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.icu.text.ListFormatter.Width
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.goodtech.tq.BuildConfig
import com.goodtech.tq.R
import com.goodtech.tq.ad.AdManager
import com.goodtech.tq.base.callback.DataCallback
import com.goodtech.tq.models.JuheAlarmModel
import com.lxj.xpopup.core.CenterPopupView

/**
 * com.chunjing.tq.dialog
 * 预警
 */
class RewardPopup(context: Context) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_reward
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()

        findViewById<Button>(R.id.btn_dialog_confirm).setOnClickListener {
            dismiss()
        }
    }
}