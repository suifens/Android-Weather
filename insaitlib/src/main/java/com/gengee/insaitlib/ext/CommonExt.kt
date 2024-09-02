package com.gengee.insaitlib.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Context.toast(content: String) {
    showToast(this, content)
}

fun Fragment.toast(content: String) {
    showToast(requireContext(), content)
}

fun JSONObject.getObject(name: String): JSONObject? {
    if (has(name)) {
        val nameObject = get(name)
        if (nameObject is JSONObject) {
            return nameObject
        }
    }
    return null
}

fun JSONObject.getArray(name: String): JSONArray? {
    if (has(name)) {
        val nameArray = get(name)
        if (nameArray is JSONArray) {
            return nameArray
        }
    }
    return null
}

private fun showToast(context: Context, content: String) {
    Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
}

inline fun <reified T : Activity> Activity.startActivity() {
    startActivity(Intent(this, T::class.java))
}

inline fun <reified T : Activity> Fragment.startActivity() {
    requireActivity().startActivity(Intent(requireActivity(), T::class.java))
}

inline fun <reified T : Activity> Activity.startActivity(pair: Pair<String, Int>) {
    val intent = Intent(this, T::class.java)
    intent.putExtra(pair.first, pair.second)
    startActivity(intent)
}

fun CharSequence?.notEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

inline fun <reified T : Any> String.fromJson(): T {
    return Gson().fromJson(this, T::class.java)
}

/// 时间戳转换成UTC
fun Long.convertToUTC(): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val date = Date(this)
        sdf.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

@SuppressLint("SimpleDateFormat")
fun String.convertToTimestamp(): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(this)
        date?.time ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}