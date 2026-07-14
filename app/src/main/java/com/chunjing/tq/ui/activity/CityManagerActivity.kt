package com.chunjing.tq.ui.activity

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.adapter.CityManagerAdapter
import com.chunjing.tq.adapter.MyItemTouchCallback
import com.chunjing.tq.databinding.ActivityCityManagerBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.vm.CityManagerViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 编辑城市
 */
class CityManagerActivity : BaseVmActivity<ActivityCityManagerBinding, CityManagerViewModel>() {

    private val removeIds by lazy { ArrayList<String>() }
    private val sortList by lazy { ArrayList<CityEntity>() }

    private var adapter: CityManagerAdapter? = null

    lateinit var itemTouchCallback: MyItemTouchCallback

    override fun bindView() = ActivityCityManagerBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
    }

    override fun initView() {

        configStationBar(mBinding.stationBar)
        mBinding.backButton.setOnClickListener { finish() }
        BarUtils.setStatusBarLightMode(this, true)

        mBinding.doneBtn.setOnClickListener {
            lifecycleScope.launch {
                val hasRemove = removeIds.isNotEmpty()
                val hasSort = sortList.isNotEmpty()
                if (!hasRemove && !hasSort) {
                    finish()
                    return@launch
                }
                val currentOrder = adapter?.snapshot().orEmpty()
                withContext(Dispatchers.IO) {
                    if (hasRemove) {
                        viewModel.removeCities(removeIds)
                    }
                    when {
                        hasSort -> viewModel.updateCitiesOrder(sortList)
                        hasRemove -> viewModel.updateCitiesOrder(currentOrder)
                    }
                }
                mainViewModel.awaitCitiesCacheRefresh(forcePost = true)
                finish()
            }
        }

        itemTouchCallback = MyItemTouchCallback(this)

        adapter = CityManagerAdapter {
            this.sortList.clear()
            this.sortList.addAll(it)
        }

        adapter!!.listener = object : CityManagerAdapter.OnCityRemoveListener {
            override fun onCityRemove(pos: Int) {
                mBinding.recyclerView.closeMenu()
                val removed = adapter?.removeAt(pos) ?: return
                removeIds.add(removed.cityId)
            }

            override fun onCityDeleteClick(pos: Int) {
                lifecycleScope.launch {
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
            adapter?.submitList(it)
        }
    }

    override fun initData() {
        viewModel.getCities()
    }
}
