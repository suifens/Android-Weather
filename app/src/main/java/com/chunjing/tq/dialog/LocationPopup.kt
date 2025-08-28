package com.chunjing.tq.dialog

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.chunjing.tq.R
import com.lxj.xpopup.core.BottomPopupView

/**
 * com.chunjing.tq.dialog
 * 定位权限弹窗
 */
class LocationPopup(context: Context) : BottomPopupView(context) {

    private var listener: OnClickListener? = null
    private lateinit var mTitleTv: TextView
    private lateinit var mMessageTv: TextView
    private lateinit var mConfirmBtn: Button
    private lateinit var mCancelBtn: Button

    var mTitle: String = "开启定位服务"
    var mMessage: String = "若您不进行授权，我们将无法提供\n精准定位下的天气服务"
    var mConfirmTest = "开启定位"
    var mCancelTest: String? = null

    override fun getImplLayoutId(): Int = R.layout.dialog_location

    override fun onCreate() {
        super.onCreate()

        mTitleTv = findViewById(R.id.tv_dialog_title)
        mTitleTv.text = mTitle
        mMessageTv = findViewById(R.id.tv_dialog_message)
        mMessageTv.text = mMessage
        mConfirmBtn = findViewById(R.id.btn_dialog_confirm)
        mConfirmBtn.text = mConfirmTest
        mCancelBtn = findViewById(R.id.btn_dialog_cancel)
        mCancelTest?.let {
            mCancelBtn.text = it
        }

        findViewById<Button>(R.id.btn_dialog_confirm).setOnClickListener {
            listener?.onClick(it)
            dismiss()
        }
        findViewById<Button>(R.id.btn_dialog_cancel).setOnClickListener {
            dismiss()
        }
    }

    fun setListener(listener: OnClickListener) {
        this.listener = listener
    }

}