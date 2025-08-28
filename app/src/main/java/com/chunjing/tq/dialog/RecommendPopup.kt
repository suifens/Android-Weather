package com.chunjing.tq.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Button
import android.widget.ImageView
import com.chunjing.tq.R
import com.lxj.xpopup.core.CenterPopupView

/**
 * com.chunjing.tq.dialog
 * 推荐弹窗
 */
class RecommendPopup(context: Context) : CenterPopupView(context) {

    private var listener: OnClickListener? = null
    fun setListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_recommend
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()

        findViewById<ImageView>(R.id.closeImgV).setOnClickListener {
            dismiss()
        }

        findViewById<Button>(R.id.shareBtn).setOnClickListener {
            listener?.onClick(it)
        }
    }


}