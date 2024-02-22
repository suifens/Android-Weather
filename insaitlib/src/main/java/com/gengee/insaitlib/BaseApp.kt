package com.gengee.insaitlib

import android.app.Application
import android.content.Context
import android.view.Gravity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.blankj.utilcode.util.ToastUtils
import com.tencent.mmkv.MMKV
import kotlin.properties.Delegates

/**
 * com.gengee.insaitlib
 */
open class BaseApp : Application(), ViewModelStoreOwner {

    private lateinit var mAppViewModelStore: ViewModelStore
    private var mFactory: ViewModelProvider.Factory? = null

    companion object {
        var context: Context by Delegates.notNull()
            private set
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