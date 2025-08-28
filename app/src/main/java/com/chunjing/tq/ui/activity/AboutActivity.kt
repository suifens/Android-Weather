package com.chunjing.tq.ui.activity

import android.content.Intent
import com.chunjing.tq.databinding.ActivityAboutBinding
import com.chunjing.tq.ui.base.BaseActivity
import com.umeng.analytics.MobclickAgent

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    override fun bindView() = ActivityAboutBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
    }

    override fun initView() {
        configStationBar(mBinding.privateStationBar)
        mBinding.backButton.setOnClickListener { finish() }
    }

    override fun initEvent() {

    }

    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

}