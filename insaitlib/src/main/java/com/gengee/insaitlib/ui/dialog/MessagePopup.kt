package com.gengee.insaitlib.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gengee.insaitlib.R
import com.lxj.xpopup.core.CenterPopupView

/**
 * 设置翻页时间
 */
class MessagePopup(context: Context) : CenterPopupView(context), OnClickListener {

    private var mTitle: CharSequence? = null
    private var mMessage: CharSequence? = null
    private var mImageId = 0
    private var mCancelable = true
    private var mShowIgnore = false
    private var mIsIgnore = false
    private var mIgnoreTypeImgV: ImageView? = null

    private var mCancelText: CharSequence? = null
    private var mConfirmText: CharSequence? = null
    private var mConfirmBg = 0
    private var mCancelBg = 0
    private var mConfirmTextColor = 0
    private var mCancelTextColor = 0
    private var mDismissAfterClick = true

    private var mShowClose = false
    private var mIgnoreText: CharSequence? = null

    private var mConfirmListener: (() -> Unit)? = null
    private var mCancelListener: (() -> Unit)? = null
    private var mIgnoreListener: (() -> Unit)? = null

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_message
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onCreate() {
        super.onCreate()

        val mLogoImgV = findViewById<ImageView>(R.id.img_dialog_logo)
        val mTitleTv = findViewById<TextView>(R.id.tv_dialog_title)
        val mMessageTv = findViewById<TextView>(R.id.tv_dialog_message)
        val mCancelBtn = findViewById<Button>(R.id.btn_dialog_cancel)
        val mIgnoreView = findViewById<View>(R.id.layout_ignore)
        mIgnoreView?.setOnClickListener(this)
        val mIgnoreTv = findViewById<TextView>(R.id.tv_ignore)
        mCancelBtn?.setOnClickListener(this)
        val mConfirmBtn = findViewById<Button>(R.id.btn_dialog_confirm)
        mConfirmBtn?.setOnClickListener(this)
        mIgnoreTypeImgV = findViewById(R.id.img_ignore_type)

        val closeImgV = findViewById<ImageView>(R.id.closeImgV)
        if (mShowClose && closeImgV != null) {
            closeImgV.visibility = VISIBLE
            closeImgV.setOnClickListener(this)
        }

        if (mMessage != null && mMessage?.length!! > 0) {
            mMessageTv.visibility = VISIBLE
            mMessageTv.text = mMessage
        } else {
            mMessageTv.visibility = GONE
        }

        if (mTitle != null && mTitle?.length!! > 0) {
            mTitleTv.visibility = VISIBLE
            mTitleTv.text = mTitle
        } else {
            mTitleTv.visibility = GONE
        }

        if (mImageId != 0) {
            mLogoImgV.visibility = VISIBLE
            mLogoImgV.setImageResource(mImageId)
        } else {
            mLogoImgV.visibility = GONE
        }

        if (mCancelText != null) {
            mCancelBtn!!.text = mCancelText
        }

        if (mConfirmText != null) {
            mConfirmBtn!!.text = mConfirmText
        }

        if (mConfirmBg != 0) {
            mConfirmBtn!!.setBackgroundResource(mConfirmBg)
        }

        if (mCancelBg != 0) {
            mCancelBtn!!.setBackgroundResource(mCancelBg)
        }

        if (mConfirmTextColor != 0) {
            mConfirmBtn!!.setTextColor(mConfirmTextColor)
        }

        if (mCancelTextColor != 0) {
            mCancelBtn!!.setTextColor(mCancelTextColor)
        }
        mCancelBtn!!.visibility = if (mCancelable) VISIBLE else GONE

        mIgnoreView!!.visibility = if (mShowIgnore) VISIBLE else GONE
        if (mIgnoreText != null) {
            mIgnoreTv.text = mIgnoreText
        }
        changeIgnore(mIsIgnore)
    }

    fun setTitle(@StringRes titleId: Int) {
        setTitle(context.getString(titleId))
    }

    fun setTitle(title: CharSequence?) {
        mTitle = title
    }

    fun setMessage(@StringRes messageId: Int) {
        setMessage(context.getString(messageId))
    }

    fun setMessage(message: CharSequence?) {
        mMessage = message
    }

    /**
     * 取消按钮文案
     */
    fun setCancelText(@StringRes textId: Int) {
        setCancelText(context.getString(textId))
    }

    fun setCancelText(text: CharSequence?) {
        mCancelText = text
    }

    fun setCancelBackground(bgRes: Int) {
        mCancelBg = bgRes
    }

    fun setCancelTextColor(color: Int) {
        mCancelTextColor = color
    }

    /**
     * 确定按钮文案
     */
    fun setConfirmText(@StringRes textId: Int) {
        setConfirmText(context.getString(textId))
    }

    fun setConfirmText(text: CharSequence?) {
        mConfirmText = text
    }

    fun setConfirmBackground(bgRes: Int) {
        mConfirmBg = bgRes
    }

    fun setConfirmTextColor(color: Int) {
        mConfirmTextColor = color
    }

    fun setIconImage(@DrawableRes resId: Int) {
        mImageId = resId
    }

    //  设置不再提醒文案
    fun setIgnoreText(text: CharSequence?) {
        mIgnoreText = text
    }

    fun setShowIgnore(show: Boolean) {
        mShowIgnore = show
    }

    fun isIgnore(): Boolean {
        return mIsIgnore
    }

    fun setIgnore(isIgnore: Boolean) {
        mIsIgnore = isIgnore
    }

    fun setShowClose(showClose: Boolean) {
        mShowClose = showClose
    }

    /**
     * 取消回调
     */
    fun setCancelListener(cancelListener: (() -> Unit)?) {
        mCancelListener = cancelListener
    }

    fun setConfirmListener(confirmListener: (() -> Unit)?) {
        mConfirmListener = confirmListener
    }

    fun setIgnoreListener(ignoreListener: (() -> Unit)?) {
        mIgnoreListener = ignoreListener
    }

    /**
     * 是否显示取消按钮
     */
    fun setCancelable(cancelable: Boolean) {
        mCancelable = cancelable
    }

    /**
     * 点击后是否自动dismiss
     */
    fun setDismissAfterClick(dismissAfterClick: Boolean) {
        mDismissAfterClick = dismissAfterClick
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_dialog_cancel) {
            mCancelListener?.invoke()
            if (mDismissAfterClick) dismiss()
        } else if (id == R.id.btn_dialog_confirm) {
            mConfirmListener?.invoke()
            if (mDismissAfterClick) dismiss()
        } else if (id == R.id.layout_ignore) {
            changeIgnore(!mIsIgnore)
            mIgnoreListener?.invoke()
        } else if (id == R.id.closeImgV) {
            dismiss()
        }
    }

    private fun changeIgnore(ignored: Boolean) {
        mIsIgnore = ignored
        if (ignored) {
            mIgnoreTypeImgV!!.setImageResource(R.drawable.ic_circle_s)
        } else {
            mIgnoreTypeImgV!!.setImageResource(0)
        }
    }

}