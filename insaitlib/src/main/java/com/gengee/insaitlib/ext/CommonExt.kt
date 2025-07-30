package com.gengee.insaitlib.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.UUID

fun lowercaseUUID(): String {
    return UUID.randomUUID().toString().replace("-", "").lowercase()
}

/**
 * 格式化小数
 * @param value 数值
 * @param decimalPlaces 保留小数位数
 */
fun formatDouble(value: Double, decimalPlaces: Int): String {
    return if (value % 1 == 0.0) {
        value.toInt().toString()
    } else {
        val pattern = if (decimalPlaces > 0) {
            "0." + "0".repeat(decimalPlaces)
        } else {
            "0"
        }
        val decimalFormat = DecimalFormat(pattern)
        decimalFormat.format(value)
    }
}

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