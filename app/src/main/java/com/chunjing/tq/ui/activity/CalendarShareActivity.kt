package com.chunjing.tq.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import coil.load
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PermissionUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.calendarVM
import com.chunjing.tq.databinding.ActivityShareCalendarBinding
import com.chunjing.tq.databinding.StubShareSelectBinding
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.ext.ShareType
import com.chunjing.tq.ext.shotView
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseActivity
import com.chunjing.tq.utils.ShareFileUtils
import com.goodtech.weatherlib.utils.ImageTools
import com.goodtech.weatherlib.utils.WeatherUtils
import com.umeng.analytics.MobclickAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarShareActivity : BaseActivity<ActivityShareCalendarBinding>() {

    private lateinit var shareBinding: StubShareSelectBinding

    private var weatherBean: WeatherBean? = null
    private var bgPath: String? = null

    companion object {
        fun startActivity(context: Context, bgPath: String?) {
            val intent = Intent(context, CalendarShareActivity::class.java)
            if (bgPath != null) intent.putExtra( "bgPath", bgPath)
            context.startActivity(intent)
        }
    }

    override fun bindView() = ActivityShareCalendarBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
        intent?.let {
            bgPath = it.getStringExtra("bgPath")
        }
    }

    override fun initView() {
        configStationBar(mBinding.stationBar)
        BarUtils.setStatusBarLightMode(this, true)
        shareBinding = StubShareSelectBinding.bind(mBinding.root)
    }

    override fun initEvent() {
        mBinding.backButton.setOnClickListener { finish() }

        shareBinding.layoutShareQq.setOnClickListener {
            shareBtnPressed(ShareType.QQ)
        }

        shareBinding.layoutShareWechat.setOnClickListener {
            shareBtnPressed(ShareType.WeChat)
        }

        shareBinding.layoutShareWechatMoment.setOnClickListener {
            shareBtnPressed(ShareType.WeChatMoments)
        }

        shareBinding.layoutShareMore.setOnClickListener {
            shareBtnPressed(ShareType.More)
        }

        calendarVM.dayDetail.observe(this) {
            mBinding.tvTime.text = "农历${it.lunar} • ${it.weekday}"
        }
    }

    override fun initData() {
        val calendar = Calendar.getInstance()
        mBinding.tvDay.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
        mBinding.tvMonth.text = "${calendar.get(Calendar.MONTH) + 1}月"

        val sdf = SimpleDateFormat("yyyy-M-d")
        calendarVM.getDayDetails(sdf.format(calendar.time))

        mainViewModel.curLocation.value?.let {
            mBinding.cityNameTv.text = it.mergerName
            mBinding.ivLoc.visibility = View.VISIBLE
        }

        mBinding.weatherBgImgV.load(bgPath, imageLoader) {
            placeholder(R.drawable.img_bg)
        }

        mainViewModel.loadWeather(LOCATION_ID) { weather ->
            weather?.let {
                mBinding.imgWeather.load(WeatherUtils.getIcon(it.getIconCd()))
                mBinding.tvWeather.text = "${it.getCurrentTemp()}°"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
        //  删除分享图片
        if (mShareImgPath != null && mShareImgPath!!.isNotEmpty()) {
            FileUtils.delete(mShareImgPath).let { success ->
                if (success) {
                    mShareImgPath = null
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }


    private var mShareImgPath: String? = null
    /**
     * 分享按钮点击
     */
    private fun shareBtnPressed(shareType: ShareType) {
        val permission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        PermissionUtils.permission(
            permission, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                showLoading()
                getScreenShotBitmap() {
                    mShareImgPath = ImageTools.saveImageToStorages(context, saveBitmap)
                    mShareImgPath?.let { path ->
                        ShareFileUtils.shareImage(context, shareType, path)
                    }
                }
            }
            override fun onDenied() {
            }
        }).request()
    }

    private var saveBitmap: Bitmap? = null
    private fun getScreenShotBitmap(action: (agree: Boolean) -> Unit = {}) {
        if (saveBitmap != null && !saveBitmap!!.isRecycled) {
//            saveBitmap!!.recycle()
            saveBitmap = null
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(100L)
            shotView(mBinding.shareLayout, this@CalendarShareActivity.window) {
                saveBitmap = it
                action.invoke(true)
            }
        }
    }

}