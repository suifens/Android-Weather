package com.goodtech.tq.views.popup

import android.annotation.SuppressLint
import android.content.Context
import com.goodtech.tq.R
import com.lxj.xpopup.core.DrawerPopupView
import com.lxj.xpopup.core.PositionPopupView

/**
 */
class TopTitlePopup(context: Context) : PositionPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_top_title
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onCreate() {
        super.onCreate()
        isCreated = true
    }

}