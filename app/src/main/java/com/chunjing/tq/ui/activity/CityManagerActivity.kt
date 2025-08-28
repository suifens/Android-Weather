package com.chunjing.tq.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.ItemTouchHelper
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.adapter.CityManagerAdapter
import com.chunjing.tq.adapter.MyItemTouchCallback
import com.chunjing.tq.bean.MessageEvent
import com.chunjing.tq.databinding.ActivityCityManagerBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ui.activity.vm.CityManagerViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 * 编辑城市
 */
@SuppressLint("NotifyDataSetChanged")
class CityManagerActivity : BaseVmActivity<ActivityCityManagerBinding, CityManagerViewModel>() {

    private val datas by lazy { ArrayList<CityEntity>() }
    private val removeIds by lazy { ArrayList<String>() }
    private val sortList by lazy { ArrayList<CityEntity>() }

    private var adapter: CityManagerAdapter? = null

    //    @Inject
    lateinit var itemTouchCallback: MyItemTouchCallback

    override fun bindView() = ActivityCityManagerBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
    }

    override fun initView() {

        configStationBar(mBinding.stationBar)
        mBinding.backButton.setOnClickListener { finish() }
        BarUtils.setStatusBarLightMode(this, true)

        //  完成编辑
        mBinding.doneBtn.setOnClickListener {
            if (removeIds.size > 0) {
                for (cityId in removeIds) {
                    viewModel.removeCity(cityId)
                }
                EventBus.getDefault().post(MessageEvent(cityChanged = true))
            }
            if (sortList.size > 0) {
                viewModel.updateCities(sortList)
                EventBus.getDefault().post(MessageEvent(cityChanged = true))
            }
            finish()
        }

        itemTouchCallback = MyItemTouchCallback(this)

        adapter = CityManagerAdapter(datas) {
            this.sortList.clear()
            this.sortList.addAll(it)
        }

        adapter!!.listener = object : CityManagerAdapter.OnCityRemoveListener {
            override fun onCityRemove(pos: Int) {
                if (pos >= datas.size) return
                mBinding.recyclerView.closeMenu()

                removeIds.add(datas[pos].cityId)
                datas.removeAt(pos)
                adapter?.notifyDataSetChanged()
            }

            override fun onCityDeleteClick(pos: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    mBinding.recyclerView.showDeleteMenu(pos, SizeUtils.dp2px(80f))
                }
            }
        }

        mBinding.recyclerView.adapter = adapter

        mBinding.recyclerView.setStateCallback {
            itemTouchCallback.dragEnable = it
        }

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(mBinding.recyclerView)
    }

    override fun initEvent() {
        viewModel.cities.observe(this) {
            datas.clear()
            datas.addAll(it)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun initData() {
        viewModel.getCities()
    }
}