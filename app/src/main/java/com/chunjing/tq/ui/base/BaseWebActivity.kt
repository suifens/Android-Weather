package com.chunjing.tq.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chunjing.tq.databinding.ActivityBaseWebBinding
import com.umeng.analytics.MobclickAgent

class BaseWebActivity : BaseActivity<ActivityBaseWebBinding>() {

    private var mUrl: String? = null
    private var mTitle: String? = null
    private var mChannel: String? = null

    companion object {
        private const val EXTRA_LINK = "link"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_CHANNEL = "channel"

        fun startActivity(context: Context, link: String, title: String?, channel: String?) {
            val intent = Intent(context, BaseWebActivity::class.java)
            intent.putExtra(EXTRA_LINK, link)
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_CHANNEL, channel)
            context.startActivity(intent)
        }
    }

    override fun bindView() = ActivityBaseWebBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
        intent?.let {
            mUrl = it.getStringExtra(EXTRA_LINK)
            mTitle = it.getStringExtra(EXTRA_TITLE)
            mChannel = it.getStringExtra(EXTRA_CHANNEL)
        }
    }

    override fun initView() {

        configStationBar(mBinding.privateStationBar)

        mBinding.backButton.setOnClickListener {
            if (mBinding.webView.canGoBack()) {
                mBinding.webView.goBack()
            } else {
                finish()
            }
        }

        mBinding.titleTv.text = mTitle

        configWebView(mBinding.webView)

        mBinding.webView.webViewClient = object : WebViewClient() {
            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }
    }

    override fun initEvent() {
    }

    override fun initData() {

    }


    override fun onStart() {
        super.onStart()
        if (!TextUtils.isEmpty(mChannel)) {
            MobclickAgent.onPageStart(mChannel)
            MobclickAgent.onResume(this)
        }

        mUrl?.let {
            if (mBinding.webView.url != null) {
                mBinding.webView.reload()
            } else {
//                TipHelper.showProgressDialog(this)
                mBinding.webView.loadUrl(it)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        if (!TextUtils.isEmpty(mChannel)) {
            MobclickAgent.onPageEnd(mChannel)
            MobclickAgent.onPause(this)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && mBinding.webView.canGoBack()) {
            mBinding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configWebView(webView: WebView) {
        val webSettings = webView.settings
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.loadsImagesAutomatically = true
        // 设置与Js交互的权限
        webSettings.javaScriptEnabled = true
        //  打开本地缓存
        webSettings.domStorageEnabled = true
        //不缓存
        // webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.allowFileAccess = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.setSupportZoom(true)
        try {
            val clazz: Class<*> = webSettings.javaClass
            val method = clazz.getMethod(
                "setAllowUniversalAccessFromFileURLs",
                Boolean::class.javaPrimitiveType
            )
            method.invoke(webSettings, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.isLongClickable = true
        webView.settings.textZoom = 100
        webView.setOnLongClickListener { v: View? -> true }
        webView.webViewClient = object : WebViewClient() {
            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
//                TipHelper.dismissProgressDialog()
            }
        }
    }

}