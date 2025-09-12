package com.gengee.insaitlib.ext

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.lxj.xpopup.XPopup
import org.jetbrains.annotations.NotNull

/**
 * 设置view显示
 */
fun View.visible() {
    visibility = View.VISIBLE
}


/**
 * 设置view占位隐藏
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrGone(flag:Boolean) {
    visibility = if(flag){
        View.VISIBLE
    }else{
        View.GONE
    }
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrInvisible(flag:Boolean) {
    visibility = if(flag){
        View.VISIBLE
    }else{
        View.INVISIBLE
    }
}

/**
 * 设置view隐藏
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 将view转为bitmap
 */
@Deprecated("use View.drawToBitmap()")
fun View.toBitmap(scale: Float = 1f, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
    if (this is ImageView) {
        if (drawable is BitmapDrawable) return (drawable as BitmapDrawable).bitmap
    }
    this.clearFocus()
    val bitmap = createBitmapSafely(
        (width * scale).toInt(),
        (height * scale).toInt(),
        config,
        1
    )
    if (bitmap != null) {
        Canvas().run {
            setBitmap(bitmap)
            save()
            drawColor(Color.WHITE)
            scale(scale, scale)
            this@toBitmap.draw(this)
            restore()
            setBitmap(null)
        }
    }
    return bitmap
}

fun createBitmapSafely(width: Int, height: Int, config: Bitmap.Config, retryCount: Int): Bitmap? {
    try {
        return Bitmap.createBitmap(width, height, config)
    } catch (e: OutOfMemoryError) {
        e.printStackTrace()
        if (retryCount > 0) {
            System.gc()
            return createBitmapSafely(width, height, config, retryCount - 1)
        }
        return null
    }
}


/**
 * 防止重复点击事件 默认0.5秒内不可重复点击
 * @param interval 时间间隔 默认0.5秒
 * @param action 执行方法
 */
var lastClickTime = 0L
fun View.clickNoRepeat(interval: Long = 500, action: (view: View) -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (lastClickTime != 0L && (currentTime - lastClickTime < interval)) {
            return@setOnClickListener
        }
        lastClickTime = currentTime
        action(it)
    }
}


fun Any?.notNull(notNullAction:(value:Any) ->Unit,nullAction1:() ->Unit){
    if(this!=null){
        notNullAction.invoke(this)
    }else{
        nullAction1.invoke()
    }
}

// ==================== 系统栏适配相关扩展方法 ====================

/**
 * 为View添加状态栏内边距适配
 * 适用于Android 15 (API 35)及以上版本
 * 自动为View添加状态栏高度的内边距，避免内容被状态栏遮挡
 * 
 * @param view 需要适配的View，如果为null则不做任何操作
 * 
 * 使用场景：
 * - 全屏Activity中需要避开状态栏的View
 * - 沉浸式状态栏下的内容区域
 * - 需要动态适配不同设备状态栏高度的场景
 */
fun View.insetStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val stateBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(stateBars.left, stateBars.top, stateBars.right, stateBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}

/**
 * 为View添加导航栏内边距适配
 * 适用于Android 15 (API 35)及以上版本
 * 自动为View添加导航栏高度的内边距，避免内容被导航栏遮挡
 * 
 * @param view 需要适配的View，如果为null则不做任何操作
 * 
 * 使用场景：
 * - 全屏Activity中需要避开导航栏的View
 * - 沉浸式导航栏下的内容区域
 * - 需要动态适配不同设备导航栏高度的场景
 */
fun View.insetNavigationBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, navigationBars.top, navigationBars.right, navigationBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}

/**
 * 为View添加系统栏底部内边距适配
 * 适用于Android 15 (API 35)及以上版本
 * 只添加系统栏的左右和底部内边距，顶部不添加内边距
 * 
 * @param view 需要适配的View，如果为null则不做任何操作
 * 
 * 使用场景：
 * - 顶部有自定义状态栏处理的View
 * - 只需要避开底部导航栏的场景
 * - 顶部使用其他方式处理状态栏适配的情况
 */
fun View.insetSystemBarBottom() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}

/**
 * 为View添加完整系统栏内边距适配
 * 适用于Android 15 (API 35)及以上版本
 * 自动为View添加状态栏和导航栏的完整内边距，确保内容不被系统栏遮挡
 * 
 * @param view 需要适配的View，如果为null则不做任何操作
 * 
 * 使用场景：
 * - 需要完整避开所有系统栏的View
 * - 全屏沉浸式界面中的主要内容区域
 * - 需要同时处理状态栏和导航栏适配的场景
 * 
 * 注意：此方法会同时处理状态栏和导航栏，如果只需要处理其中一种，
 * 建议使用 insetStatusBar() 或 insetNavigationBar() 方法
 */
fun View.insetSystemBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
