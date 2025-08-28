package com.goodtech.weatherlib.dialog

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.goodtech.weatherlib.BaseApp
import com.goodtech.weatherlib.R
import com.goodtech.weatherlib.databinding.DialogLoadingBinding
import kotlinx.coroutines.*

/**
 * 自定义加载进度对话框
 */
class LoadingDialog(context: Context) : BaseDialog<DialogLoadingBinding?>(context, 0.38f, 0f) {
    var loadingDrawable: AnimationDrawable? = null
    private var scope: CoroutineScope? = null

    override fun bindView(): DialogLoadingBinding {
        return DialogLoadingBinding.inflate(layoutInflater)
    }

    override fun initView() {
        mBinding?.ivLoading?.load(R.drawable.loading)
//        loadingDrawable = mBinding?.ivLoading?.background as AnimationDrawable?
    }

    override fun initEvent() {}

    fun setTip(tip: String?) {
        if (!tip.isNullOrEmpty()) {
            mBinding?.tvLoadingTip?.text = tip
        }
    }

    override fun show() {
        super.show()
        if (loadingDrawable != null) {
            if (scope == null) {
                scope = CoroutineScope(Job() + Dispatchers.Main)
                scope?.launch {
                    (loadingDrawable as Animatable).start()
                }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        scope?.cancel()
        scope = null
        if (loadingDrawable != null) {
            (loadingDrawable as Animatable).stop()
        }
    }
}