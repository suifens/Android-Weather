package com.goodtech.tq.modules.video

import android.content.Context
import android.util.Log
import com.bytedance.sdk.dp.DPSdk
import com.bytedance.sdk.dp.IDPWidget
import com.bytedance.sdk.dp.DPWidgetDrawParams
import com.bytedance.sdk.dp.IDPDrawListener

/**
 * 视频工具类，用于安全地处理视频相关操作
 */
object VideoUtils {
    private const val TAG = "VideoUtils"

    /**
     * 安全地创建视频组件
     */
    fun createVideoWidgetSafely(
        context: Context?,
        params: DPWidgetDrawParams?,
        listener: IDPDrawListener?
    ): IDPWidget? {
        return try {
            if (context == null) {
                Log.w(TAG, "Context为空，无法创建视频组件")
                return null
            }

            if (!DPSdk.isStartSuccess()) {
                Log.w(TAG, "DP SDK未启动，无法创建视频组件")
                return null
            }

            if (!DPHolder.isDPStarted) {
                Log.w(TAG, "DP SDK未完全启动，无法创建视频组件")
                return null
            }

            val widget = DPHolder.buildDrawWidget(params)
            if (widget == null) {
                Log.e(TAG, "创建视频组件失败")
                return null
            }

            Log.d(TAG, "视频组件创建成功")
            widget
        } catch (e: Exception) {
            Log.e(TAG, "创建视频组件时发生异常", e)
            null
        }
    }

    /**
     * 检查视频SDK是否可用
     */
    fun isVideoSDKAvailable(): Boolean {
        return try {
            DPSdk.isStartSuccess() && DPHolder.isDPStarted
        } catch (e: Exception) {
            Log.e(TAG, "检查视频SDK状态时发生异常", e)
            false
        }
    }

    /**
     * 安全地执行视频相关操作
     */
    fun executeVideoOperationSafely(operation: () -> Unit) {
        try {
            operation()
        } catch (e: Exception) {
            Log.e(TAG, "执行视频操作时发生异常", e)
        }
    }
} 