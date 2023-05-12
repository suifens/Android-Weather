package com.goodtech.tq.views.popup

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.goodtech.tq.R
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.OnConfirmListener

/**
 * com.chunjing.tq.dialog
 * 升级提醒
 */
class UpdatePopup(context: Context) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_update
    }

    private var version = ""
    var confirmListener: OnConfirmListener? = null

    fun setupVersion(version: String, listener: OnConfirmListener) {
        this.version = version
        this.confirmListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()

        if (version.isNotEmpty()) {
            findViewById<TextView>(R.id.versionTv).text = "发现新版本 V$version"
        }

        findViewById<Button>(R.id.updateBtn).setOnClickListener {
            if (confirmListener != null) {
                confirmListener!!.onConfirm()
            }
            dismiss()
        }

        findViewById<ImageView>(R.id.closeBtn).setOnClickListener {
            dismiss()
        }
    }


}