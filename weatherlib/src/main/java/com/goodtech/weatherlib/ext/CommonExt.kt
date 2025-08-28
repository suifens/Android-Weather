package com.goodtech.weatherlib.ext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat

/**
 * 获取屏幕宽度
 */
val Context.screenWidth
    get() = resources.displayMetrics.widthPixels

/**
 * 获取屏幕高度
 */
val Context.screenHeight
    get() = resources.displayMetrics.heightPixels

/**
 * 判断是否为空 并传入相关操作
 */
inline fun <reified T> T?.notNull(notNullAction: (T) -> Unit, nullAction: () -> Unit = {}) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction.invoke()
    }
}

/**
 * 判断是否为空 并传入相关操作
 */
/*fun <T> Any?.notNull(f: () -> T, t: () -> T): T {
    return if (this != null) f() else t()
}*/




/**
 * dp值转换为px
 */
fun Context.dp2px(dp: Int): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

/**
 * px值转换成dp
 */
fun Context.px2dp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

/**
 * dp值转换为px
 */
fun View.dp2px(dp: Int): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

/**
 * px值转换成dp
 */
fun View.px2dp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

/**
 * 复制文本到粘贴板
 */
fun Context.copyToClipboard(text: String, label: String = "JetpackMvvm") {
    val clipData = ClipData.newPlainText(label, text)
    val clipboardManager = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboardManager?.setPrimaryClip(clipData)
}

/**
 * 检查是否启用无障碍服务
 */
fun Context.checkAccessibilityServiceEnabled(serviceName: String): Boolean {
    val settingValue =
        Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
    var result = false
    val splitter = TextUtils.SimpleStringSplitter(':')
    while (splitter.hasNext()) {
        if (splitter.next().equals(serviceName, true)) {
            result = true
            break
        }
    }
    return result
}

/**
 * 设置点击事件
 * @param views 需要设置点击事件的view
 * @param onClick 点击触发的方法
 */
fun setOnclick(vararg views: View?, onClick: (View) -> Unit) {
    views.forEach {
        it?.setOnClickListener { view ->
            onClick.invoke(view)
        }
    }
}

/**
 * 设置防止重复点击事件
 * @param views 需要设置点击事件的view集合
 * @param interval 时间间隔 默认0.5秒
 * @param onClick 点击触发的方法
 */
fun setOnclickNoRepeat(vararg views: View?, interval: Long = 500, onClick: (View) -> Unit) {
    views.forEach {
        it?.clickNoRepeat(interval = interval) { view ->
            onClick.invoke(view)
        }
    }
}

fun String.toHtml(flag: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(this, flag)
    } else {
        Html.fromHtml(this)
    }
}

/**
 * 输入框获取焦点，弹出键盘
 */
fun EditText.showSoftInput(context: Context) {
    this.requestFocus()
    this.postDelayed({
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(this, 0)
    }, 200)
}

/**
 * 降雨率
 */
fun String.getRainfall(): Int {
    if (this.contains("暴雨") || this.contains("暴雪")) {
        return 100
    }
    if (this.contains("大雨") || this.contains("大雪")) {
        return 90
    }
    if (this.contains("中雨") || this.contains("中雪")) {
        return 80
    }
    if (this.contains("阵雨") || this.contains("阵雪")) {
        return 50
    }
    if (this.contains("雨") || this.contains("雪")) {
        return 70
    }
    if (this.contains("阴")) {
        return 20
    }
    return 0
}


