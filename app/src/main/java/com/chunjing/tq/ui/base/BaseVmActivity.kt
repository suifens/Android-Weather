package com.chunjing.tq.ui.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

/**
 * 带 [ViewBinding] 与 [ViewModel] 的 Activity 基类。
 * 子类需实现 [bindView]（例如 `XxxBinding.inflate(layoutInflater)`）。
 */
abstract class BaseVmActivity<T : ViewBinding, V : ViewModel> : BaseActivity<T>() {

    protected lateinit var viewModel: V
        private set

    override fun init() {
        viewModel = ViewModelProvider(this)[getViewModelClass()]
        super.init()
    }

    protected open fun getViewModelClass(): Class<V> =
        ViewModelTypeResolver.resolve(this, BaseVmActivity::class.java)

    abstract override fun bindView(): T

    companion object {
        /** 与 [Context.LOCATION_SERVICE] 一致，供扩展等通过 `BaseVmActivity.LOCATION_SERVICE` 引用 */
        const val LOCATION_SERVICE: String = Context.LOCATION_SERVICE
    }
}
