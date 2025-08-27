package com.goodtech.tq.utils

import android.content.Context
import android.util.Log
import com.goodtech.tq.app.App

/**
 * 权限管理工具类
 * 控制SDK初始化时机，避免在用户同意权限前请求应用列表权限
 */
object PermissionManager {
    
    private const val TAG = "PermissionManager"
    
    /**
     * 检查用户是否已同意隐私政策
     */
    fun isPrivacyAgreed(context: Context): Boolean {
        return SpUtils.getInstance().isAgreePermission()
    }
    
    /**
     * 用户同意隐私政策后调用
     * 初始化所有可能请求权限的SDK
     */
    fun onPrivacyAgreed(context: Context) {
        Log.d(TAG, "用户同意隐私政策，开始初始化SDK")
        
        // 设置权限同意标记
        SpUtils.getInstance().setPermissionAgree(true)
        
        // 延迟初始化SDK，确保权限同意状态已保存
        App.instance.startUsingApp(context as? android.app.Activity)
    }
    
    /**
     * 检查是否可以初始化SDK
     */
    fun canInitializeSDK(context: Context): Boolean {
        return isPrivacyAgreed(context)
    }
    
    /**
     * 安全初始化SDK
     * 只有在用户同意权限后才初始化
     */
    fun safeInitializeSDK(context: Context) {
        if (canInitializeSDK(context)) {
            Log.d(TAG, "权限已同意，安全初始化SDK")
            App.instance.startUsingApp(context as? android.app.Activity)
        } else {
            Log.d(TAG, "权限未同意，跳过SDK初始化")
        }
    }
    
    /**
     * 获取权限状态信息
     */
    fun getPermissionStatus(context: Context): String {
        return if (isPrivacyAgreed(context)) {
            "已同意隐私政策"
        } else {
            "未同意隐私政策"
        }
    }
}
