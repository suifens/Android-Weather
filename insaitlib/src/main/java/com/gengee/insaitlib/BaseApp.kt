package com.gengee.insaitlib

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.view.Gravity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.blankj.utilcode.util.ToastUtils
import com.lxj.xpopup.impl.LoadingPopupView
import com.tencent.mmkv.MMKV
import kotlin.properties.Delegates

/**
 * com.gengee.insaitlib
 */
open class BaseApp : Application(), ViewModelStoreOwner {

    private lateinit var mAppViewModelStore: ViewModelStore
    private var mFactory: ViewModelProvider.Factory? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var loadingDialog: LoadingPopupView? = null
        var context: Context by Delegates.notNull()
            private set

        fun resetLoadingDialog() {
            loadingDialog?.destroy()
            loadingDialog = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        mAppViewModelStore = ViewModelStore()

        initToast()
    }

    private fun initToast()
    { //Kotlin中
        val defaultMaker = ToastUtils.getDefaultMaker()
        defaultMaker.setGravity(Gravity.CENTER,0,0)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            // 执行清理工作，例如释放资源或保存数据
            performCleanup()
        }
    }

    private fun performCleanup() {
        // 在这里实现清理工作
        loadingDialog?.destroy()
    }

    /**
     * 获取一个全局的ViewModel
     */
    fun getAppViewModelProvider(): ViewModelProvider {
        return ViewModelProvider(this, this.getAppFactory())
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        if (mFactory == null) {
            mFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        }
        return mFactory as ViewModelProvider.Factory
    }

    override val viewModelStore: ViewModelStore
        get() = mAppViewModelStore

}