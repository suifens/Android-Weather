package com.chunjing.tq.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.adapter.LifeListAdapter
import com.chunjing.tq.bean.LifeEntity
import com.chunjing.tq.databinding.ActivityLifeBinding
import com.chunjing.tq.dialog.LifeDetailsPopup
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.vm.LifeViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import com.goodtech.weatherlib.view.GridSpaceItemDecoration
import com.lxj.xpopup.XPopup

class LifeActivity : BaseVmActivity<ActivityLifeBinding, LifeViewModel>() {

    private var mAdapter: LifeListAdapter? = null

    override fun bindView() = ActivityLifeBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {

    }

    override fun initView() {
        configStationBar(mBinding.privateStationBar)
        mBinding.backButton.setOnClickListener { finish() }

        val lifeEntity = LifeEntity()
        mAdapter = LifeListAdapter(lifeEntity, true) { it ->
            val popup = LifeDetailsPopup(this)
            popup.lifeDetails = it
            val entity = this.viewModel.lifeLiveData.value
            if (entity != null) {
                popup.lifeTitle = entity.lifeTitleWith(it)
                popup.lifeImgRes = entity.lifeImageWith(it)
            }
            mainViewModel.curCity.value?.let { city ->
                popup.cityName = if (city.isLocal()) city.mergerName else city.cityName
            }
            mainViewModel.curWeather.value?.let { weather ->
                popup.observation = weather.observation
            }

            XPopup.Builder(this)
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(popup)
                .show()
        }

        mBinding.recyclerView.adapter = mAdapter

        //  添加间距
        val layoutManager = GridLayoutManager(context, 2)
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.addItemDecoration(
            GridSpaceItemDecoration(
                2,
                SizeUtils.dp2px(15f),
//                SizeUtils.dp2px(25f)
                (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(150f) * 2)/3
            )
        )

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initEvent() {
        viewModel.lifeLiveData.observe(this) {
            it?.let {
                mAdapter?.mData = it
                mAdapter?.notifyDataSetChanged()
            }
        }

    }

    override fun initData() {
        mainViewModel.curCity.value?.let { city ->
            viewModel.getLifeDetails(city)
        }
    }

}