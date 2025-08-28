package com.chunjing.tq.ext

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.PixelCopy
import android.view.View
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.PermissionUtils
import com.chunjing.tq.MyApp
import com.chunjing.tq.R
import com.chunjing.tq.dialog.PermissionPopup
import com.chunjing.tq.widget.service.WidgetServiceDouble
import com.chunjing.tq.widget.service.WidgetServiceSmall
import com.chunjing.tq.ui.base.BaseVmActivity
import com.chunjing.tq.ui.base.BaseWebActivity
import com.chunjing.tq.utils.ContentUtil
import com.chunjing.tq.utils.ShareFileUtils
import com.goodtech.weatherlib.ext.toHtml
import com.goodtech.weatherlib.utils.ImageTools
import com.goodtech.weatherlib.view.viewpager.ScaleTransitionPagerTitleView
import com.lxj.xpopup.XPopup
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

//  友盟appid
const val UM_APP_ID = "63747e9905844627b5824c88"

//  微信AppID
const val WECHAT_APP_ID = "wx3f8523f5f86afa2e"

//  天气提醒
const val REMINDER_WEATHER = "ReminderWeather"

//  定位时间
const val LAST_LOCATION_TIME = "LAST_LOCATION_TIME"

/**
 * 天气获取 latitude/longitude
 */
const val WEATHER_URL = "https://api.weather.com/v1/geocode/%s/%s/aggregate.json?language=zh-CN" +
        "&apiKey=e45ff1b7c7bda231216c7ab7c33509b8&products=conditionsshort,fcstdaily10short,fcsthourly24short,nowlinks"

// <editor-fold default-state="collapsed" desc="聚合">
//  运势
const val JUHE_FORTUNE =
    "http://web.juhe.cn/constellation/getAll?consName=%s&type=%s&key=a31e488d8a2cf98b0ad401625bbe7892"

//  万年历 当天详细信息
const val JUHE_DAY_DETAIL =
    "http://v.juhe.cn/calendar/day?date=%s&key=3fb26fed72acf7a7e4154d3a55f196c7"

//  万年历 近期假期
const val JUHE_MONTH_HOLIDAY =
    "http://v.juhe.cn/calendar/month?year-month=%s&key=3fb26fed72acf7a7e4154d3a55f196c7"

//  老黄历 日历
const val JUHE_DAY_ALMANAC =
    "http://v.juhe.cn/laohuangli/d?date=%s&key=8795e207bb1d403376908835ae90a7ea"

//  老黄历 时辰
const val JUHE_HOURS_ALMANAC =
    "http://v.juhe.cn/laohuangli/h?date=%s&key=8795e207bb1d403376908835ae90a7ea"

//  天气预报 根据城市查询生活指数
const val JUHE_LIFE =
    "http://apis.juhe.cn/simpleWeather/life?city=%s&key=9af0b9d910cf209a708ba81a2d694012"

//  天气预报 根据城市查询天气
const val JUHE_QUERY =
    "http://apis.juhe.cn/simpleWeather/query?city=%s&key=9af0b9d910cf209a708ba81a2d694012"

//  预警
const val JUHE_ALARM =
    "https://apis.juhe.cn/fapig/alarm/queryV2?key=c6f53b5c5b62866f2f5b3a604a8fd81b&city_code=%s"

//  心灵鸡汤
const val JUHE_SOUL: String = "https://apis.juhe.cn/fapig/soup/query?key=766fc4828d8963b8ca8e64d68c6de100"

// </editor-fold>


//  隐私
const val LINK_PRIVACY = "https://app.yiguxm.com/privacy/chunjingPrivacy.html"

//  隐私清单
const val LINK_PRIVACY_LIST = "https://app.yiguxm.com/privacy/chunjingyinsiqingdan.html"

//  用户协议
const val LINK_AGREEMENT = "https://app.yiguxm.com/privacy/chunjingyonghuxieyi.html"

//  台风
const val LINK_TAIFENG = "http://typhoon.nmc.cn/mobile.html"

//  疫情出行
const val LINK_CAILING = "https://iring.diyring.cc/friend/7adfedeaef801c3d#/"

val MILLIS_DAY = 24 * 60 * 60 * 1000


//绑定普通的Recyclerview
fun RecyclerView.init(
    layoutManger: RecyclerView.LayoutManager,
    bindAdapter: RecyclerView.Adapter<*>,
    isScroll: Boolean = true
): RecyclerView {
    layoutManager = layoutManger
    setHasFixedSize(true)
    adapter = bindAdapter
    isNestedScrollingEnabled = isScroll
    return this
}

/**
 * 检查是否同意了隐私权限
 */
fun FragmentActivity.checkPermissionAgree(action: () -> Unit = {}) {
    if (!ContentUtil.permissionGranted) {
        showPermissionDialog { action.invoke() }
    } else {
        action.invoke()
    }
}

fun FragmentActivity.showPermissionDialog(action: (agree: Boolean) -> Unit = {}) {
    val context = this
    val popup = PermissionPopup(context)
    popup.setListener(object : PermissionPopup.PermissionPopupListener {
        override fun onConfirmClick() {
            ContentUtil.permissionGranted = true
            MyApp.instance().configUM()
            action.invoke(true)
        }

        override fun onAgreementClick() {
            BaseWebActivity.startActivity(
                context,
                LINK_AGREEMENT, "用户协议", "agreement"
            )
        }

        override fun onPrivateClick() {
            BaseWebActivity.startActivity(
                context,
                LINK_PRIVACY, "隐私协议", "private"
            )
        }

    })
    XPopup.Builder(context)
        .isDestroyOnDismiss(true)   //  只使用一次
        .asCustom(popup)
        .show()
}

fun FragmentActivity.startWidgetService() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

        PermissionUtils.permission(
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
        ).callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(this@startWidgetService)) {
                    startForegroundService(Intent(this@startWidgetService, WidgetServiceSmall::class.java))
                    startForegroundService(Intent(this@startWidgetService, WidgetServiceDouble::class.java))
                } else {
                    startService(Intent(this@startWidgetService, WidgetServiceSmall::class.java))
                    startService(Intent(this@startWidgetService, WidgetServiceDouble::class.java))
                }
            }
            override fun onDenied() {
            }
        }).request()
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(this)) {
            startForegroundService(Intent(this, WidgetServiceSmall::class.java))
            startForegroundService(Intent(this, WidgetServiceDouble::class.java))
        } else {
            startService(Intent(this, WidgetServiceSmall::class.java))
            startService(Intent(this, WidgetServiceDouble::class.java))
        }
    }
}

fun FragmentActivity.shotView(view: View, window: Window, bitmapCallback: (Bitmap) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Above Android O, use PixelCopy
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        PixelCopy.request(
            window,
            Rect(
                location[0],
                location[1],
                location[0] + view.width,
                location[1] + view.height
            ),
            bitmap,
            {
                if (it == PixelCopy.SUCCESS) {
                    bitmapCallback.invoke(bitmap)
                }
            },
            Handler(Looper.getMainLooper())
        )
    } else {
        val tBitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.RGB_565
        )
        val canvas = Canvas(tBitmap)
        view.draw(canvas)
        canvas.setBitmap(null)
        bitmapCallback.invoke(tBitmap)
    }
}

/**
 * 检查GPS权限
 */
fun FragmentActivity.checkGPSPermission(): Boolean {
    val pm1 = PermissionUtils.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    val pm2 = PermissionUtils.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    return (pm1 || pm2)
}

/**
 * 检查GPS状态
 */
fun FragmentActivity.checkGPSOpen(): Boolean {
    val locationManager = getSystemService(BaseVmActivity.LOCATION_SERVICE) as LocationManager
    val pr1 = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val pr2 = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    return (pr1 || pr2)
}

fun MagicIndicator.bindViewPager2(
    viewPager: ViewPager2,
    mStringList: List<String> = arrayListOf(),
    action: (index: Int) -> Unit = {}
) {
    val commonNavigator = CommonNavigator(context)
    commonNavigator.adapter = object : CommonNavigatorAdapter() {

        override fun getCount(): Int {
            return mStringList.size
        }

        override fun getTitleView(context: Context, index: Int): IPagerTitleView {
            return ScaleTransitionPagerTitleView(context).apply {
                //设置文本
                text = mStringList[index].toHtml()
                //字体大小
                textSize = 20f
                //未选中颜色
                normalColor = resources.getColor(R.color.black_30)
                //选中颜色
                selectedColor = resources.getColor(R.color.black)
                //点击事件
                setOnClickListener {
                    viewPager.currentItem = index
                    action.invoke(index)
                }
            }
        }

        override fun getIndicator(context: Context): IPagerIndicator {
            return LinePagerIndicator(context).apply {
                mode = LinePagerIndicator.MODE_EXACTLY
                //线条的宽高度
                lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                lineWidth = UIUtil.dip2px(context, 30.0).toFloat()
                //线条的圆角
//                roundRadius = UIUtil.dip2px(context, 6.0).toFloat()
                startInterpolator = AccelerateInterpolator()
                endInterpolator = DecelerateInterpolator(2.0f)
                //线条的颜色
                setColors(resources.getColor(R.color.color_red))
            }
        }
    }
    this.navigator = commonNavigator

    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            this@bindViewPager2.onPageSelected(position)
            action.invoke(position)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            this@bindViewPager2.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            this@bindViewPager2.onPageScrollStateChanged(state)
        }
    })
}

fun ViewPager2.init(
    activity: FragmentActivity,
    fragments: ArrayList<Fragment>,
    isUserInputEnabled: Boolean = true
): ViewPager2 {
    //是否可滑动
    this.isUserInputEnabled = isUserInputEnabled
    //设置适配器
    adapter = object : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int) = fragments[position]
        override fun getItemCount() = fragments.size
    }
    return this
}

fun ViewPager2.init(
    fragment: Fragment,
    fragments: ArrayList<Fragment>,
    isUserInputEnabled: Boolean = true
): ViewPager2 {
    //是否可滑动
    this.isUserInputEnabled = isUserInputEnabled
    //设置适配器
    adapter = object : FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int) = fragments[position]
        override fun getItemCount() = fragments.size
    }
    return this
}

fun GradientDrawable.init(
    startColor: Int,
    endColor: Int,
    direction: GradientDrawable.Orientation
): GradientDrawable? {
    return try {
        val colorsArray = intArrayOf(startColor, endColor)
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            colors = colorsArray
            gradientType = GradientDrawable.LINEAR_GRADIENT
            orientation = direction
        }
    } catch (e: Exception) {
        null
    }
}


/**
 * 隐藏软键盘
 */
fun hideSoftKeyboard(activity: Activity?) {
    activity?.let { act ->
        val view = act.currentFocus
        view?.let {
            val inputMethodManager =
                act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}

///**
// * 加载列表数据
// */
//fun <T> loadListData(
//    data: ListDataUiState<T>,
//    baseQuickAdapter: BaseQuickAdapter<T, *>,
//    loadService: LoadService<*>,
//    recyclerView: SwipeRecyclerView,
//    swipeRefreshLayout: SwipeRefreshLayout
//) {
//    swipeRefreshLayout.isRefreshing = false
//    recyclerView.loadMoreFinish(data.isEmpty, data.hasMore)
//    if (data.isSuccess) {
//        //成功
//        when {
//            //第一页并没有数据 显示空布局界面
//            data.isFirstEmpty -> {
//                loadService.showEmpty()
//            }
//            //是第一页
//            data.isRefresh -> {
//                baseQuickAdapter.setList(data.listData)
//                loadService.showSuccess()
//            }
//            //不是第一页
//            else -> {
//                baseQuickAdapter.addData(data.listData)
//                loadService.showSuccess()
//            }
//        }
//    } else {
//        //失败
//        if (data.isRefresh) {
//            //如果是第一页，则显示错误界面，并提示错误信息
//            loadService.showError(data.errMessage)
//        } else {
//            recyclerView.loadMoreError(0, data.errMessage)
//        }
//    }
//}