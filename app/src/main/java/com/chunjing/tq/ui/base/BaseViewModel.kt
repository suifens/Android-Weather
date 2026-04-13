package com.chunjing.tq.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chunjing.tq.BuildConfig
import com.goodtech.weatherlib.net.LoadState
import com.goodtech.weatherlib.net.exception.ExceptionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

open class BaseViewModel : ViewModel() {

    // 加载状态
    val loadState = MutableLiveData<LoadState>()

    /**
     * 是否登录
     */
    val isLogin = MutableLiveData<Boolean>()

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
                val error = ExceptionUtils.parseException(e)
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                if (loadingType == 0) {
                    loadState.value = LoadState.Error(error)
                    if (runningCount.get() > 0) {
                        runningCount.getAndDecrement()
                    }
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
