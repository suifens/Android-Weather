package com.chunjing.tq.modules.removeAd.view

import android.annotation.SuppressLint
import com.chunjing.tq.R
import android.content.Context
import android.widget.Button
import com.lxj.xpopup.core.CenterPopupView

/**
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