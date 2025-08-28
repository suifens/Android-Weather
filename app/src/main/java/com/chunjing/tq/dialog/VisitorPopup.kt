package com.chunjing.tq.dialog

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chunjing.tq.R
import com.lxj.xpopup.core.BottomPopupView

/**
 * com.chunjing.tq.dialog
 */
class VisitorPopup(context: Context) : BottomPopupView(context) {

    private var listener: VisitorPopupListener? = null
    private lateinit var visitorMsgTv: TextView

    override fun getImplLayoutId(): Int = R.layout.dialog_visitor

    override fun onCreate() {
        super.onCreate()

        visitorMsgTv = findViewById(R.id.visitorMsgTv)
        findViewById<Button>(R.id.btn_dialog_confirm).setOnClickListener {
            listener?.onConfirmClick()
        }
        findViewById<Button>(R.id.btn_dialog_visitor).setOnClickListener {
            listener?.onVisitorClick()
        }
        findViewById<Button>(R.id.btn_dialog_cancel).setOnClickListener {
            dismiss()
            listener?.onCancelClick()
        }

        configSpannable()
    }

    fun setListener(listener: VisitorPopupListener) {
        this.listener = listener
    }

    private fun configSpannable() {
        
        val permissionStr: String = context.getString(R.string.msg_disagree)
        val spannableString = SpannableString(permissionStr)
        val agreementStr: String = context.getString(R.string.permission_agreement)
        val agreementStart = permissionStr.indexOf(agreementStr)
        val agreementEnd = agreementStart + agreementStr.length
        val privateStr: String = context.getString(R.string.permission_private)
        val privateStart = permissionStr.indexOf(privateStr)
        val privateEnd = privateStart + privateStr.length
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                listener?.onAgreementClick()
            }
        }
        spannableString.setSpan(
            clickableSpan,
            agreementStart,
            agreementEnd,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        val privateClickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                listener?.onPrivateClick()
            }
        }
        spannableString.setSpan(
            privateClickable,
            privateStart,
            privateEnd,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        spannableString.setSpan(object : UnderlineSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context, R.color.color_theme) //设置颜色
                ds.isUnderlineText = false //去掉下划线
            }
        }, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spannableString.setSpan(object : UnderlineSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context, R.color.color_theme) //设置颜色
                ds.isUnderlineText = false //去掉下划线
            }
        }, privateStart, privateEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        visitorMsgTv.movementMethod = LinkMovementMethod.getInstance()
        visitorMsgTv.text = spannableString
    }

    interface VisitorPopupListener {
        fun onConfirmClick()
        fun onCancelClick()
        fun onAgreementClick()
        fun onPrivateClick()
        fun onVisitorClick()
    }

}