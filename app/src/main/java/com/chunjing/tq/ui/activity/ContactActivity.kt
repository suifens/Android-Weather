package com.chunjing.tq.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.BarUtils
import com.chunjing.tq.databinding.ActivityContactBinding
import com.chunjing.tq.databinding.ActivityWidgetSettingBinding
import com.chunjing.tq.ui.base.BaseActivity
import com.umeng.analytics.MobclickAgent

class ContactActivity : BaseActivity<ActivityContactBinding>() {

    override fun bindView() = ActivityContactBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
    }

    override fun initView() {
        configStationBar(mBinding.privateStationBar)
        mBinding.backButton.setOnClickListener { finish() }
        BarUtils.setStatusBarLightMode(this, true)
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