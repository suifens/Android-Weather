package com.gengee.insaitlib.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView

//loading框
@SuppressLint("StaticFieldLeak")
private var loadingDialog: LoadingPopupView? = null

/**
 * 打开等待框
 */
fun Activity.showLoadingExt(message: String = "请求网络中", dismissOnTouchOutside: Boolean = false) {
    if (!this.isFinishing) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if (loadingDialog == null || loadingDialog!!.popupInfo == null) {
                loadingDialog?.dismiss()
                loadingDialog = XPopup.Builder(this)
                    .isDestroyOnDismiss(true)
                    .isLightNavigationBar(true)
                    .hasNavigationBar(false)
                    .shadowBgColor(Color.parseColor("#33000000"))
                    .dismissOnTouchOutside(dismissOnTouchOutside)   //  不可点击隐藏弹窗
                    .asLoading(null, LoadingPopupView.Style.ProgressBar)
            }
            loadingDialog?.setTitle(message)
            loadingDialog?.show()
        }
    }
}

/**
 * 打开等待框
 */
fun Fragment.showLoadingExt(message: String = "请求网络中") {
    this.requireActivity().showLoadingExt(message)
}

/**
 * 关闭等待框
 */
fun Activity.dismissLoadingExt() {
    loadingDialog?.let {
        it.dismiss()
        loadingDialog = null
    }
}

/**
 * 关闭等待框
 */
fun Activity.dismissLoadingExt(message: String, delay: Long = 2000L) {
    loadingDialog?.let {
        it.setTitle(message)
        it.delayDismiss(delay)
    }
}

/**
 * 关闭等待框
 */
fun Fragment.dismissLoadingExt() {
    loadingDialog?.let {
        it.dismiss()
        loadingDialog = null
    }
}

fun Fragment.dismissLoadingExt(message: String, delay: Long = 2000L) {
    loadingDialog?.let {
        it.setTitle(message)
        it.delayDismiss(delay)
    }
}
