package com.goodtech.tq.views.popup

import android.annotation.SuppressLint
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
class AlarmPopup(context: Context) : CenterPopupView(context) {

    private var mAlarmBean: JuheAlarmModel? = null
    private var adContainer: CardView? = null
    private var feedContainer: FrameLayout? = null
    private var mFeedAd: TTFeedAd? = null

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

        adContainer = findViewById(R.id.ad_contentPanel)
        if (adContainer != null) {
            feedContainer = findViewById(R.id.feed_container)
        }
    }

    override fun onShow() {
        super.onShow()
        loadFeedAd()
    }

    private fun loadFeedAd() {
        feedContainer?.let { container ->
            val width = SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat()) - 120
            AdManager.getInstance().loadFeedAd(context as android.app.Activity, BuildConfig.PGE_CALENDAR_POS_ID, width, object : DataCallback<TTFeedAd> {
                override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
                    if (data != null) {
                        container.visibility = View.VISIBLE
                        container.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        mFeedAd = data
                        AdManager.getInstance().showFeedAd(context as android.app.Activity, container, data)
                    } else {
                        container.visibility = View.GONE
                        container.layoutParams.height = 0
                    }
                }
            })
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        mFeedAd?.destroy()
        mFeedAd = null
        feedContainer?.removeAllViews()
        feedContainer = null
    }
}