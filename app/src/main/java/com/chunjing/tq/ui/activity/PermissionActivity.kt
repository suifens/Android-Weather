package com.chunjing.tq.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.BarUtils
import com.chunjing.tq.MyApp
import com.chunjing.tq.R
import com.chunjing.tq.databinding.ActivityPermissionBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.dialog.TravelPopup
import com.chunjing.tq.dialog.VisitorPopup
import com.chunjing.tq.ext.LINK_AGREEMENT
import com.chunjing.tq.ext.LINK_PRIVACY
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseActivity
import com.chunjing.tq.ui.base.BaseWebActivity
import com.chunjing.tq.utils.ContentUtil
import com.lxj.xpopup.XPopup

/**
 * 权限弹窗
 */
class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {

    override fun bindView() = ActivityPermissionBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {

    }

    override fun initView() {
        // 沉浸式态栏
        immersionStatusBar()

        BarUtils.setStatusBarLightMode(this, true)

        initSpannable()

        //  同意
        mBinding.agreeBtn.setOnClickListener {
            startToAddCity()
        }
        //  取消
        mBinding.disagreeBtn.setOnClickListener {
            val visitorPopup = VisitorPopup(this)
            visitorPopup.setListener(object : VisitorPopup.VisitorPopupListener {
                override fun onConfirmClick() {
                    onStartWeather(false)
                }

                override fun onCancelClick() {
                    finish()
                }

                override fun onAgreementClick() {
                    BaseWebActivity.startActivity(
                        this@PermissionActivity,
                        LINK_AGREEMENT, "用户协议", "agreement"
                    )
                }

                override fun onPrivateClick() {
                    BaseWebActivity.startActivity(
                        this@PermissionActivity,
                        LINK_PRIVACY, "隐私协议", "private"
                    )
                }

                override fun onVisitorClick() {
                    onStartWeather(true)
                }

            })
            XPopup.Builder(this)
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(visitorPopup)
                .show()
        }
    }

    override fun initEvent() {

    }

    override fun initData() {

    }

    @SuppressLint("SuspiciousIndentation")
    private fun onStartWeather(isVisitor: Boolean) {
        ContentUtil.permissionGranted = !isVisitor
        if (isVisitor) {
            showVisitor()
        } else {
            startToAddCity()
        }
    }

    private fun startToAddCity() {
        ContentUtil.permissionGranted = true
        MyApp.instance().configUM()
        AddCityActivity.startActivity(this, true)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "onResume: ${ContentUtil.travelCity}")
        if (travelPopup != null && ContentUtil.travelCity != null) {
            travelPopup!!.setupTravelCity(ContentUtil.travelCity!!)
            ContentUtil.travelCity = null
        }
    }

    private var travelPopup: TravelPopup? = null

    //  游客
    private fun showVisitor() {
        showLoading()

        val cityMode = CityEntity()
        cityMode.cityId = "110000"
        cityMode.cityCode = "010"
        cityMode.mergerName = "北京"
        cityMode.cityName = "北京"
        cityMode.shortName = "北京"
        cityMode.latitude = "39.904989"
        cityMode.longitude = "116.405285"
        cityMode.pinyin = "Beijing"
        mainViewModel.addCity(cityMode)
//        startActivity<MainActivity>()
        MainActivity.startActivity(this@PermissionActivity, 0)
        finish()
    }

    /// 配置 spannable
    private fun initSpannable() {
        mBinding.spannableTv.let {
            val permissionStr = resources.getString(R.string.permission_title1)
            var spannableString = SpannableString(permissionStr)

            val agreementStr = resources.getString(R.string.permission_agreement)
            val agreementStart: Int = permissionStr.indexOf(agreementStr)
            val agreementEnd: Int = agreementStart + agreementStr.length

            val privateStr = resources.getString(R.string.permission_private)
            val privateStart: Int = permissionStr.indexOf(privateStr)
            val privateEnd: Int = privateStart + privateStr.length

            val boldStr = resources.getString(R.string.permission_bold)
            val boldStart: Int = permissionStr.indexOf(boldStr)
            val boldEnd: Int = boldStart + boldStr.length

            //  点击跳转
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        BaseWebActivity.startActivity(
                            this@PermissionActivity,
                            LINK_PRIVACY, "隐私协议", "private"
                        )
                    }
                },
                agreementStart,
                agreementEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )

            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        BaseWebActivity.startActivity(
                            this@PermissionActivity,
                            LINK_AGREEMENT, "用户协议", "agreement"
                        )
                    }
                },
                privateStart,
                privateEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )

            spannableString.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = resources.getColor(R.color.color_theme) //设置颜色
                    ds.isUnderlineText = false //去掉下划线
                }
            }, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            spannableString.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = resources.getColor(R.color.color_theme) //设置颜色
                    ds.isUnderlineText = false //去掉下划线
                }
            }, privateStart, privateEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            //粗体
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                boldStart,
                boldEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )

            it.movementMethod = LinkMovementMethod.getInstance()
            it.text = spannableString
        }
    }

}