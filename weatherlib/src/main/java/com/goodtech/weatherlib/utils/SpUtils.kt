package com.goodtech.weatherlib.utils

import androidx.annotation.Nullable
import com.tencent.mmkv.MMKV

/**
 * 存储工具类
 */
class SpUtils {
    
    private var preferences: MMKV? = null
    
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SpUtils()
        }
    }
    
    init {
        preferences = MMKV.defaultMMKV()
    }

    // </editor-fold>
    fun putInt(key: String, value: Int): SpUtils {
        preferences!!.encode(key, value)
        return this
    }

    fun getInt(key: String, dValue: Int): Int {
        return preferences!!.decodeInt(key, dValue)
    }

    fun putLong(key: String, value: Long): SpUtils {
        preferences!!.encode(key, value)
        return this
    }

    fun getLong(key: String, dValue: Long?): Long {
        return preferences!!.decodeLong(key, dValue!!)
    }

    fun putFloat(key: String, value: Float): SpUtils {
        preferences!!.encode(key, value)
        return this
    }

    fun getFloat(key: String, dValue: Float?): Float {
        return preferences!!.decodeFloat(key, dValue!!)
    }

    fun putBoolean(key: String, value: Boolean): SpUtils {
        preferences!!.encode(key, value)
        return this
    }

    fun getBoolean(key: String, dValue: Boolean): Boolean {
        return preferences!!.decodeBool(key, dValue)
    }

    fun putString(key: String, value: String): SpUtils {
        preferences!!.encode(key, value)
        return this
    }

    fun getString(key: String, dValue: String): String {
        return preferences!!.decodeString(key, dValue)!!
    }

    fun remove(key: String) {
        preferences!!.removeValueForKey(key)
    }

}