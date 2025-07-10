package com.goodtech.tq.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.goodtech.tq.R

class SettingFragment : BaseFragment() {

    override fun getViewLayoutRes(): Int = R.layout.fragment_setting

    override fun setupCacheViews() {
        super.setupCacheViews()
        
        // 这里可以添加设置页面的具体功能
        // 比如：主题设置、通知设置、关于我们等
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置页面初始化
        mCacheView.findViewById<TextView>(R.id.tv_setting_title).text = "设置"
    }
} 