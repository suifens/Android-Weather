package com.goodtech.tq.views.popup

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Button
import android.widget.TextView
import com.goodtech.tq.R
import com.goodtech.tq.models.JuheAlarmModel
import com.lxj.xpopup.core.CenterPopupView

/**
 * com.chunjing.tq.dialog
 * 预警
 */
class AlarmPopup(context: Context) : CenterPopupView(context) {

    private var mAlarmBean: JuheAlarmModel? = null

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_alarm
    }

    fun setupData(alarm: JuheAlarmModel) {
        mAlarmBean = alarm
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()

        mAlarmBean?.let {
            val titleTv = findViewById<TextView>(R.id.titleTv)
            titleTv.text = "${it.type}${it.level}预警"

            val messageTv = findViewById<TextView>(R.id.tv_dialog_message)
            messageTv.text = it.content
        }

        findViewById<Button>(R.id.btn_dialog_confirm).setOnClickListener {
            dismiss()
        }
    }


}