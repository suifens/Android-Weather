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
     * 初始化推送/统计等核心 SDK（广告 SDK 延迟初始化）
     */
    fun onPrivacyAgreed(context: Context) {
        Log.d(TAG, "用户同意隐私政策")
        
        // 仅记录用户同意，核心 SDK 延迟到 Splash/Main 首屏初始化
        SpUtils.getInstance().setPermissionAgree(true)
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
            App.instance.startCoreSdk()
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
