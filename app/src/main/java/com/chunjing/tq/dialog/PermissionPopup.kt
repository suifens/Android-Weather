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
class PermissionPopup(context: Context) : BottomPopupView(context) {

    private var listener: PermissionPopupListener? = null
    private lateinit var permissionMsgTv: TextView

    override fun getImplLayoutId(): Int = R.layout.dialog_permission

    override fun onCreate() {
        super.onCreate()

        permissionMsgTv = findViewById(R.id.permissionMsgTv)
        findViewById<Button>(R.id.btn_dialog_confirm).setOnClickListener {
            listener?.onConfirmClick()
            dismiss()
        }
        findViewById<Button>(R.id.btn_dialog_cancel).setOnClickListener {
            dismiss()
        }

        configSpannable()
    }

    fun setListener(listener: PermissionPopupListener) {
        this.listener = listener
    }

    private fun configSpannable() {

        val permissionStr: String = context.getString(R.string.msg_permission)
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

        permissionMsgTv.movementMethod = LinkMovementMethod.getInstance()
        permissionMsgTv.text = spannableString
    }

    interface PermissionPopupListener {
        fun onConfirmClick()
        fun onAgreementClick()
        fun onPrivateClick()
    }

}