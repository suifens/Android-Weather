package com.goodtech.weatherlib

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.properties.Delegates

abstract class BaseApp : Application(), ViewModelStoreOwner {

    private var mFactory: ViewModelProvider.Factory? = null

//    val viewmodelStore: ViewModelStore
//        get() = ViewModelStore()

    companion object {
        var context: Context by Delegates.notNull()
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = this
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

//    override fun getViewModelStore(): ViewModelStore {
//        return viewmodelStore
//    }

}