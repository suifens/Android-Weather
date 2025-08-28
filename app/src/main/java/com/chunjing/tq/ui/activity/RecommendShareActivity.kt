package com.chunjing.tq.ui.activity

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import coil.load
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PermissionUtils
import com.chunjing.tq.R
import com.chunjing.tq.databinding.ActivityWeatherRecommendBinding
import com.chunjing.tq.databinding.StubShareSelectBinding
import com.chunjing.tq.ext.ShareType
import com.chunjing.tq.ext.shotView
import com.chunjing.tq.ui.base.BaseActivity
import com.chunjing.tq.utils.ShareFileUtils
import com.chunjing.tq.utils.ShareHelper
import com.goodtech.weatherlib.utils.ImageTools
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.umeng.analytics.MobclickAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecommendShareActivity : BaseActivity<ActivityWeatherRecommendBinding>() {

    private lateinit var shareBinding: StubShareSelectBinding
    private lateinit var shareHelper: ShareHelper

    override fun bindView() = ActivityWeatherRecommendBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
    }

    override fun initView() {
        configStationBar(mBinding.stationBar)
        BarUtils.setStatusBarLightMode(this, true)
        shareHelper = ShareHelper(this)
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
    }

    override fun initData() {
        mBinding.shareImgV.load(R.drawable.share_recommend)
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
                    var shared = true
                    when (shareType) {
                        ShareType.WeChat ->
                            shared = shareHelper.shareImgToWx(saveBitmap, SendMessageToWX.Req.WXSceneSession)
                        ShareType.WeChatMoments ->
                            shared = shareHelper.shareImgToWx(saveBitmap, SendMessageToWX.Req.WXSceneTimeline)
//                        ShareType.QQ -> {
//                            mShareImgPath = ImageTools.saveImageToGallery(context, saveBitmap)
//                            mShareImgPath?.let { path ->
//                                shareHelper.shareToQQ(path)
//                            }
//                        }
                        else -> {
                            mShareImgPath = ImageTools.saveImageToStorages(context, saveBitmap)
                            mShareImgPath?.let { path ->
                                if (shareType == ShareType.QQ) {
                                    shared = shareHelper.shareToQQ(path)
                                } else {
                                    ShareFileUtils.shareImage(context, shareType, path)
                                }
                            }
                        }
                    }
                    if (!shared) dismissLoading()
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
            shotView(mBinding.shareContainer, this@RecommendShareActivity.window) {
                saveBitmap = it
                action.invoke(true)
            }
        }
    }

}