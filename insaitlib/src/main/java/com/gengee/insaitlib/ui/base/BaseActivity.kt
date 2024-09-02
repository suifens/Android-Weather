package com.gengee.insaitlib.ui.base

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.gengee.insaitlib.ext.dismissLoadingExt
import com.gengee.insaitlib.ext.showLoadingExt
import me.hgj.jetpackmvvm.base.activity.BaseVmVbActivity

/**
 * 描述　: 你项目中的Activity基类，在这里实现显示弹窗，吐司，还有加入自己的需求操作 ，如果不想用 Databind，请继承
 * BaseVmActivity例如
 * abstract class BaseActivity<VM : BaseViewModel> : BaseVmActivity<VM>() {
 */
abstract class BaseActivity<VM : MyBaseViewModel, VB : ViewBinding> : BaseVmVbActivity<VM, VB>() {

    protected val mHandler: Handler = Handler(Looper.getMainLooper())
    protected var isFirstLoad = true

    abstract override fun initView(savedInstanceState: Bundle?)

    /**
     * 创建liveData观察者
     */
    override fun createObserver() { }

    /**
     * 接收数据
     * @param intent
     */
    open fun onPrepareData(intent: Intent?) {  }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 初始化数据
        onPrepareData(intent)
    }

    /**
     * 打开等待框
     */
    override fun showLoading(message: String) {
        showLoadingExt(message)
    }

    /**
     * 关闭等待框
     */
    override fun dismissLoading() {
        dismissLoadingExt()
    }

    override fun onPause() {
        super.onPause()
        dismissLoadingExt()
    }

    /**
     * 配置station bar
     */
    open fun configStationBar(stationBar: View) {
        val bars: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(stationBar.layoutParams)
        bars.height += BarUtils.getStatusBarHeight()
        stationBar.layoutParams = bars
    }

    /**
     * 沉浸式状态栏
     */
    protected open fun immersionStatusBar() {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // 沉浸式状态栏
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        // window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//实现状态栏图标和文字颜色为暗色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        // 状态栏改为透明
        window.statusBarColor = Color.TRANSPARENT
    }
}