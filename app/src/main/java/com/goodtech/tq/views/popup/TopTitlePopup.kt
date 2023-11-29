package com.goodtech.tq.views.popup

import android.annotation.SuppressLint
import android.content.Context
import com.goodtech.tq.R
import com.lxj.xpopup.core.DrawerPopupView
import com.lxj.xpopup.core.PositionPopupView
import android.widget.TextView
import com.goodtech.tq.R
import com.lxj.xpopup.core.DrawerPopupView
import com.lxj.xpopup.core.PositionPopupView
import kotlinx.android.synthetic.main.item_layout01.view.title

/**
 */
class TopTitlePopup(context: Context) : PositionPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_top_title
    }

    private lateinit var mTitleTv: TextView
    private lateinit var mMessageTv: TextView
    private var mTitle: String? = null
    private var mMessage: String? = null

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onCreate() {
        super.onCreate()
        isCreated = true

        mTitleTv = findViewById(R.id.titleTv)
        mTitle?.let { mTitleTv.text = it }
        mMessageTv = findViewById(R.id.messageTv)
        mMessage?.let { mMessageTv.text = it }
    }

    fun setupData(title: String, message: String) {
        this.mTitle = title
        this.mMessage = message
    }

}