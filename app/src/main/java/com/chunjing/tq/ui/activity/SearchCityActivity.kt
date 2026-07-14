package com.chunjing.tq.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.chunjing.tq.adapter.SearchAdapter
import com.chunjing.tq.databinding.ActivitySearchCityBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.vm.SearchViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.ext.showSoftInput
import com.goodtech.weatherlib.extension.startActivity
import kotlinx.coroutines.launch

class SearchCityActivity : BaseVmActivity<ActivitySearchCityBinding, SearchViewModel>() {

    private var searchAdapter: SearchAdapter? = null

    private val searchCities by lazy { ArrayList<CityEntity>() }

    private var isAddCity = false

    companion object {
        fun startActivity(context: Context, isAddCity: Boolean = false) {
            val intent = Intent(context, SearchCityActivity::class.java)
            intent.putExtra("addCity", isAddCity)
            context.startActivity(intent)
        }
    }

    override fun bindView() = ActivitySearchCityBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
        intent?.let {
            isAddCity = it.getBooleanExtra("addCity", false)
        }
    }

    override fun initView() {

        configStationBar(mBinding.privateStationBar)
        mBinding.cancelBtn.setOnClickListener { finish() }
        BarUtils.setStatusBarLightMode(this, true)

        searchAdapter = SearchAdapter(
            this@SearchCityActivity,
            searchCities,
            mBinding.etSearch.text.toString()
        ) {
           KeyboardUtils.hideSoftInput(this)
           if (isAddCity) {
               showLoading()
               viewModel.addCity(it)
           } else {
               ContentUtil.travelCity = it
               finish()
           }
        }
        mBinding.rvSearch.adapter = searchAdapter

        //编辑框输入监听
        mBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val keywords = mBinding.etSearch.text.toString()
                if (!TextUtils.isEmpty(keywords)) {
                    viewModel.searchCity(keywords)
                } else {
                    mBinding.rvSearch.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun initEvent() {
        viewModel.searchResult.observe(this) {
            showSearchResult(it)
        }

        //  添加城市结束：先同步刷新列表并带 pending，避免 cities 未更新时误走 showIndex=1000 且 onPause 清掉 isNewCity 后仍停在第一页
        viewModel.addFinish.observe(this) { cityId ->
            lifecycleScope.launch {
                mainViewModel.setCityId(cityId)
                mainViewModel.setPendingSelectCityTab(cityId)
                mainViewModel.awaitCitiesCacheRefresh(forcePost = true)
                MainActivity.startActivityFromAddCity(this@SearchCityActivity)
                finish()
            }
        }
    }

    override fun initData() {
    }

    override fun onResume() {
        super.onResume()
        mBinding.etSearch.showSoftInput(this.context)
    }

    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideSoftInput(this)
    }

    override fun onDestroy() {
        dismissLoading()
        super.onDestroy()
    }

    /**
     * 展示搜索结果
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun showSearchResult(basic: List<CityEntity>) {
        mBinding.rvSearch.visibility = View.VISIBLE

        searchCities.clear()

        basic.forEach { item ->
            searchCities.add(item)
        }
        searchAdapter?.notifyDataSetChanged()

        mBinding.emptyView.visibility = if (searchCities.size == 0) View.VISIBLE else View.GONE

    }

}