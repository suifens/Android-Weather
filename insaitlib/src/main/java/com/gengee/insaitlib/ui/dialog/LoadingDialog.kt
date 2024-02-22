package com.gengee.insaitlib.ui.dialog

import android.content.Context
import android.graphics.drawable.Animatable
import com.gengee.insaitlib.databinding.DialogLoadingBinding

/**
 * 自定义加载进度对话框
 */
class LoadingDialog(context: Context) : BaseDialog<DialogLoadingBinding?>(context, 0.38f, 0f) {
    override fun bindView(): DialogLoadingBinding {
        return DialogLoadingBinding.inflate(layoutInflater)
    }

    override fun initView() {
    }

    override fun initEvent() {}

    fun setTip(tip: String?) {
        if (!tip.isNullOrEmpty()) {
            mBinding?.tipTv?.text = tip
        }
    }

    override fun show() {
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
    }
}