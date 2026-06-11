package com.chunjing.tq.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PermissionUtils
import com.chunjing.tq.R
import com.chunjing.tq.databinding.FragmentSettingBinding
import com.chunjing.tq.ext.*
import com.chunjing.tq.modules.removeAd.RemoveAdActivity
import com.chunjing.tq.ui.activity.AboutActivity
import com.chunjing.tq.ui.activity.ContactActivity
import com.chunjing.tq.ui.activity.WidgetSettingActivity
import com.chunjing.tq.ui.base.BaseFragment
import com.chunjing.tq.ui.base.BaseWebActivity
import com.goodtech.weatherlib.ext.clickNoRepeat
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.utils.SpUtils

class SettingsFragment : BaseFragment<FragmentSettingBinding>() {

    private val grantedStr = "已允许"
    private val deniedStr = "权限设置"
    private val grantedColor = Color.parseColor("#9B9B9B")
    private val deniedColor = Color.parseColor("#00C4FF")

    override fun bindView() = FragmentSettingBinding.inflate(layoutInflater)

    override fun initView(view: View?) {

        mBinding.layoutVersion.imgItemIcon.setBackgroundResource(R.drawable.ic_me_version)
        mBinding.layoutVersion.tvItemName.text = "版本更新"
        mBinding.layoutVersion.itemLayout.setOnClickListener { gotoAppStores() }

        mBinding.layoutPraise.imgItemIcon.setBackgroundResource(R.drawable.ic_me_appraise)
        mBinding.layoutPraise.tvItemName.text = "欢迎评论"
        mBinding.layoutPraise.itemLayout.setOnClickListener { gotoAppStores() }

        mBinding.layoutPrivate.imgItemIcon.setBackgroundResource(R.drawable.ic_me_private)
        mBinding.layoutPrivate.tvItemName.text = "隐私协议"
        mBinding.layoutPrivate.itemLayout.setOnClickListener {
            BaseWebActivity.startActivity(requireContext(),
                LINK_PRIVACY,
                resources.getString(R.string.title_private),
                "Privacy")
        }

        mBinding.layoutAgreement.imgItemIcon.setBackgroundResource(R.drawable.ic_me_agreement)
        mBinding.layoutAgreement.tvItemName.text = "用户协议"
        mBinding.layoutAgreement.itemLayout.setOnClickListener {
            BaseWebActivity.startActivity(requireContext(),
                LINK_AGREEMENT,
                resources.getString(R.string.title_agreement),
                "Agreement")
        }

        mBinding.layoutInfoList.imgItemIcon.setBackgroundResource(R.drawable.ic_me_private)
        mBinding.layoutInfoList.tvItemName.setText(R.string.title_info_list)
        mBinding.layoutInfoList.itemLayout.setOnClickListener {
            BaseWebActivity.startActivity(requireContext(),
                "https://app.yiguxm.com/privacy/cjtqgrsjqd.html",
                resources.getString(R.string.title_info_list),
                "Info List")
        }

        mBinding.layoutShareList.imgItemIcon.setBackgroundResource(R.drawable.ic_me_private)
        mBinding.layoutShareList.tvItemName.setText(R.string.title_share_list)
        mBinding.layoutShareList.itemLayout.setOnClickListener {
            BaseWebActivity.startActivity(requireContext(),
                "https://app.yiguxm.com/privacy/cjtqgxqd.html",
                resources.getString(R.string.title_share_list),
                "Share List")
        }

        //  小组件
        mBinding.btnWidget.setOnClickListener { startActivity<WidgetSettingActivity>() }
        //  问题建议
        mBinding.btnQuestion.setOnClickListener { startActivity<ContactActivity>() }
        //  关于我们
        mBinding.btnAbout.setOnClickListener { startActivity<AboutActivity>() }
        //  隐私清单
        mBinding.btnPrivateList.setOnClickListener {
            BaseWebActivity.startActivity(requireContext(),
                LINK_PRIVACY_LIST,
                resources.getString(R.string.title_private_list),
                "Privacy_List")
        }

        //
        mBinding.layoutPermissionPhone.setOnClickListener { gotoSettings() }
        mBinding.layoutPermissionStorage.setOnClickListener { PermissionUtils.launchAppDetailsSettings() }
        //  定位
        mBinding.layoutPermissionLocation.setOnClickListener {
            val beginTransaction = requireActivity().supportFragmentManager.beginTransaction()
            beginTransaction.add(PermissionFragment.newInstance(), "permission_fragment")
            beginTransaction.commitAllowingStateLoss()
        }
        //  天气提醒
        mBinding.switchBtnReminder.setOnClickListener {
            val checked: Boolean = mBinding.switchBtnReminder.isChecked
            if (checked) {
                JPushInterface.resumePush(requireActivity())
            } else {
                JPushInterface.stopPush(requireActivity())
            }
            SpUtils.instance.putBoolean(REMINDER_WEATHER, checked)
        }
    }

    override fun initEvent() {
        mBinding.fabRemoveAd.clickNoRepeat {
            RemoveAdActivity.startActivity(requireContext())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun loadData() {
        val version = AppUtils.getAppVersionName()
        mBinding.layoutVersion.tvItemValue.text = "V $version"
    }

    override fun onResume() {
        super.onResume()

        val isChecked = SpUtils.instance.getBoolean(REMINDER_WEATHER, true)
        mBinding.switchBtnReminder.isChecked = isChecked

        val phoneStateTv: TextView = mBinding.tvStatePhone
        phoneStateTv.text = "未使用"
        phoneStateTv.setTextColor(deniedColor)

        val storageStateTv: TextView = mBinding.tvStateStorage
        if (PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
            || PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            storageStateTv.text = grantedStr
            storageStateTv.setTextColor(grantedColor)
        } else {
            storageStateTv.text = deniedStr
            storageStateTv.setTextColor(deniedColor)
        }

        val locationStateTv: TextView = mBinding.tvStateLocation
        if (requireActivity().checkGPSOpen()) {
            locationStateTv.text = grantedStr
            locationStateTv.setTextColor(grantedColor)
        } else {
            locationStateTv.text = deniedStr
            locationStateTv.setTextColor(deniedColor)
        }
    }

    private fun gotoAppStores() {
        //  评论
        val uri = Uri.parse("market://details?id=" + requireActivity().packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            //要调起的应用不存在时的处理
            Toast.makeText(requireContext(), "未能跳转到应用商店", Toast.LENGTH_SHORT).show()
        }
    }

    //  跳转到设置页面
    private fun gotoSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.parse("package:" + requireActivity().packageName)
        startActivity(intent)
    }
}