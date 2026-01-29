package com.goodtech.tq.utils

import android.util.Log

/**
 * 去广告时间管理工具类
 * 负责保存和检查去广告状态
 */
object AdRemovalManager {
    private const val TAG = "AdRemovalManager"
    
    // SharedPreferences key
    private const val KEY_AD_REMOVAL_END_TIME = "ad_removal_end_time" // 去广告结束时间戳（毫秒）
    
    /**
     * 添加去广告时长
     * @param hours 小时数（默认24小时，即1天）
     */
    fun addAdRemovalTime(hours: Int = 24) {
        val currentTime = System.currentTimeMillis()
        val endTime = currentTime + (hours * 60 * 60 * 1000L)
        
        SpUtils.getInstance().putLong(KEY_AD_REMOVAL_END_TIME, endTime)
        Log.d(TAG, "添加去广告时长: ${hours}小时，结束时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(endTime))}")
    }
    
    /**
     * 检查是否在去广告有效期内
     * @return true 如果在有效期内，false 如果已过期或未设置
     */
    fun isAdRemovalActive(): Boolean {
        val endTime = SpUtils.getInstance().getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        if (endTime == 0L) {
            return false
        }
        
        val currentTime = System.currentTimeMillis()
        val isActive = currentTime < endTime
        
        if (!isActive && endTime > 0) {
            // 已过期，清除过期数据
            SpUtils.getInstance().remove(KEY_AD_REMOVAL_END_TIME)
            Log.d(TAG, "去广告时间已过期，已清除")
        }
        
        return isActive
    }
    
    /**
     * 获取剩余去广告时长（毫秒）
     * @return 剩余时长，如果已过期或未设置则返回0
     */
    fun getRemainingTimeMillis(): Long {
        val endTime = SpUtils.getInstance().getLong(KEY_AD_REMOVAL_END_TIME, 0L)
        if (endTime == 0L) {
            return 0L
        }
        
        val currentTime = System.currentTimeMillis()
        val remaining = endTime - currentTime
        
        return if (remaining > 0) remaining else 0L
    }
    
    /**
     * 获取剩余去广告时长（天和小时）
     * @return Pair<天数, 小时数>
     */
    fun getRemainingTime(): Pair<Int, Int> {
        val remainingMillis = getRemainingTimeMillis()
        if (remainingMillis <= 0) {
            return Pair(0, 0)
        }
        
        val totalHours = (remainingMillis / (1000 * 60 * 60)).toInt()
        val days = totalHours / 24
        val hours = totalHours % 24
        
        return Pair(days, hours)
    }
    
    /**
     * 清除去广告时间
     */
    fun clearAdRemovalTime() {
        SpUtils.getInstance().remove(KEY_AD_REMOVAL_END_TIME)
        Log.d(TAG, "已清除去广告时间")
    }
}
