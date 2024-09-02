package com.gengee.insaitlib.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.gengee.insaitlib.net.LoadState
import com.gengee.insaitlib.net.exception.ExceptionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import java.util.concurrent.atomic.AtomicInteger

open class MyBaseViewModel() : BaseViewModel() {

    //  数据完成更新
    /**
     * 数据完成更新
     * true为获取到数据，false为数据未空
     */
    val dataUpdated = MutableLiveData<Boolean>()

    // 加载状态
    val loadState = MutableLiveData<LoadState>()

    /**
     * 是否登录
     */
    val isLogin = MutableLiveData<Boolean>()

//    @Volatile
//    private var runningCount = 0

    private var runningCount = AtomicInteger(0)

    /**
     * 是否正在请求网络
     */
    fun isStopped(): Boolean {
        return runningCount.get() == 0
    }

    /**
     * 后台静默加载,不显示loading
     */
    fun launchSilent(block: suspend CoroutineScope.() -> Unit) {
        launchRequest(1, block)
    }

    /**
     * 开始显示loading,结束关闭loading
     */
    fun launch(block: suspend CoroutineScope.() -> Unit) {
        launchRequest(block = block)
    }

    /**
     * @param loadingType 0: 默认 1: silent
     */
    private fun launchRequest(loadingType: Int = 0, block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch() {
            try {
                if (loadingType == 0) {
                    runningCount.getAndIncrement()
                    loadState.value = LoadState.Start()
                }
                withContext(Dispatchers.IO) {
                    block.invoke(this)
                }
            } catch (e: Throwable) {
                // handle error
                val error = ExceptionUtils.parseException(e)
                if (loadingType == 0) {
                    loadState.value = LoadState.Error(error)
                    if (runningCount.get() > 0) {
                        runningCount.getAndDecrement()
                    }
                    LogUtils.d("runningCount - : $runningCount")
                    loadState.value = LoadState.Finish
                }
            } finally {
                if (loadingType == 0) {
                    if (runningCount.get() > 0) {
                        runningCount.getAndDecrement()
                    }
                    loadState.value = LoadState.Finish
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        runningCount.getAndSet(0)
    }
}