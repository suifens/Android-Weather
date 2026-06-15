package com.goodtech.tq.modules.citySearch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.goodtech.tq.app.App
import com.goodtech.tq.helpers.DatabaseHelper
import com.goodtech.tq.models.CityMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * 城市搜索：后台查询 city.db + 防抖，避免主线程卡顿与频繁查询。
 */
object CitySearchHelper {

    private const val SEARCH_DEBOUNCE_MS = 300L

    private val jobs = mutableMapOf<LifecycleOwner, Job>()

    fun interface Callback {
        fun onResult(keyword: String, results: @JvmSuppressWildcards List<CityMode>)
    }

    @JvmStatic
    fun search(owner: LifecycleOwner, keyword: String, callback: Callback) {
        jobs[owner]?.cancel()
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        jobs[owner] = owner.lifecycleScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val trimmed = keyword.trim()
            if (trimmed.isEmpty()) {
                callback.onResult(trimmed, arrayListOf())
                return@launch
            }
            val list = withContext(Dispatchers.IO) {
                DatabaseHelper.getInstance(App.instance).queryCity(trimmed) ?: arrayListOf()
            }
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                callback.onResult(trimmed, ArrayList(list))
            }
        }
    }

    @JvmStatic
    fun cancel(owner: LifecycleOwner) {
        jobs.remove(owner)?.cancel()
    }
}
