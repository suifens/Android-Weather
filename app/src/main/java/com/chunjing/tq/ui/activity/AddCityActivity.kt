package com.chunjing.tq.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.PermissionUtils.SimpleCallback
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.adapter.PopularCityAdapter
import com.chunjing.tq.adapter.SearchAdapter
import com.chunjing.tq.databinding.ActivityAddCityBinding
import com.chunjing.tq.databinding.ActivityAddCityBinding.inflate
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.dialog.LocationPopup
import com.chunjing.tq.ext.checkGPSOpen
import com.chunjing.tq.ext.checkGPSPermission
import com.chunjing.tq.ext.startWidgetService
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.vm.SearchViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import com.chunjing.tq.ui.fragment.PermissionFragment
import com.goodtech.weatherlib.ext.clickNoRepeat
import com.goodtech.weatherlib.ext.showSoftInput
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.extension.toast
import com.goodtech.weatherlib.net.LoadState
import com.lxj.xpopup.XPopup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("NotifyDataSetChanged")
class AddCityActivity : BaseVmActivity<ActivityAddCityBinding, SearchViewModel>() {

    private var topCityAdapter: PopularCityAdapter? = null
    private val topCities by lazy { ArrayList<CityEntity>() }
    private var fromSplash = false
    private var requestedGPS = false
    private var fromLocation = false
    private var firstLocation = false

    private var searchAdapter: SearchAdapter? = null
    private val searchCities by lazy { ArrayList<CityEntity>() }

    companion object {
        fun startActivity(context: Context, fromSplash: Boolean = false) {
            val intent = Intent(context, AddCityActivity::class.java)
            intent.putExtra("fromSplash", fromSplash)
            context.startActivity(intent)
        }
    }

    override fun bindView() = inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
        intent?.let {
            fromSplash = it.getBooleanExtra("fromSplash", false)
        }
    }

    override fun initView() {

        configStationBar(mBinding.privateStationBar)
        //  返回
        mBinding.backButton.setOnClickListener { finish() }

        //  热门城市选择
        topCityAdapter = PopularCityAdapter(topCities) {
            showLoading()
            viewModel.addCity(it)
        }
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 2 else 1
            }
        }

        //  添加间距
        mBinding.rvTopCity.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view) // 获取view 在adapter中的位置。
                val columnSpacing = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(150f) * 2)/3
                if (position == 0) {
                    outRect.left = columnSpacing
                    outRect.right = columnSpacing
                    outRect.top = SizeUtils.dp2px(10f)

                } else {
                    val column = (position - 1) % 2 // view 所在的列
                    if (column == 0) {
                        outRect.left = columnSpacing   // column * (列间距 * (1f / 列数))
                        outRect.right = columnSpacing / 2
                    } else {
                        outRect.left = columnSpacing / 2  // column * (列间距 * (1f / 列数))
                        outRect.right = columnSpacing
                    }

                }
                outRect.bottom = SizeUtils.dp2px(10f)
            }
        })

        mBinding.rvTopCity.adapter = topCityAdapter
        mBinding.rvTopCity.layoutManager = layoutManager

        firstLocation = mainViewModel.curLocation.value == null

        //  配置搜索
        configSearchLayout()
        //  搜索城市
        mBinding.searchButton.setOnClickListener {
            changedSearchType(true)
        }

        if (mainViewModel.cities.value.isNullOrEmpty()) {
            mBinding.cityListTv.visibility = View.GONE
            mBinding.cityListImgV.visibility = View.GONE
        } else {
            mBinding.cityListTv.visibility = View.VISIBLE
            mBinding.cityListImgV.visibility = View.VISIBLE
        }
    }

    override fun initEvent() {

        // 定位获取的数据
        mainViewModel.curLocation.observe(this) {
            mBinding.tvCurLocation.text = it.mergerName
            mBinding.tvCurLocation.isSelected = true
            mBinding.btnRefresh.visibility = View.VISIBLE
            mBinding.btnGetPos.visibility = View.GONE
            mBinding.tvLocSubtitle.text = resources.getString(R.string.tip_location_details)

            if (fromLocation) {
                fromLocation = false
                viewModel.updateLocation(it)
                lifecycleScope.launch {
                    if (fromSplash || firstLocation) {
                        startWidgetService()
                        delay(1000L)
                        MainActivity.startActivity(this@AddCityActivity, 0)
                        finish()
                    }
                }
            }
            dismissLoading()
        }

        //  加载状态
        mainViewModel.loadState.observe(this) { showLoadState(it) }

        //  添加城市结束
        viewModel.addFinish.observe(this) {
            addFinish(it)
        }

        viewModel.topCity.observe(this) {
            showTopCity(it)
        }

        //  开启定位
        mBinding.btnGetPos.setOnClickListener {
            checkAndOpenGPS()
        }
        //  刷新定位
        mBinding.btnRefresh.setOnClickListener {
            checkAndOpenGPS()
        }
        mBinding.locationLayout.setOnClickListener {
            mainViewModel.curLocation.value?.let {
                MainActivity.startActivity(this, 0)
                finish()
            }
        }

        mBinding.cityListTv.clickNoRepeat {
            startActivity<CityListActivity>()
        }

        /**
         * 搜索
         */
        mBinding.cancelBtn.setOnClickListener {
            changedSearchType(false)
        }

        viewModel.searchResult.observe(this) {
            showSearchResult(it)
        }
    }

    override fun initData() {
        viewModel.getTopCity()
        mainViewModel.getCacheLocation()
    }

    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideSoftInput(this)
    }

    override fun onDestroy() {
        dismissLoading()
        super.onDestroy()
    }

    private fun addFinish(cityId: String) {
        lifecycleScope.launch {
            startWidgetService()

            if (fromSplash) {
                mainViewModel.showIndex.postValue(0)
                MainActivity.startActivity(this@AddCityActivity, 0)
            } else {
                mainViewModel.setCityId(cityId)
                mainViewModel.awaitCitiesCacheRefresh()
                val tabIndex = mainViewModel.cities.value.orEmpty().indexOfFirst { it.cityId == cityId }
                if (tabIndex >= 0) {
                    MainActivity.startActivity(this@AddCityActivity, tabIndex)
                } else {
                    mainViewModel.setPendingSelectCityTab(cityId)
                    MainActivity.startActivityFromAddCity(this@AddCityActivity)
                }
            }
            finish()
        }
    }

    private fun showLoadState(it: LoadState) {
        when (it) {
            is LoadState.Start -> {
                showLoading(true, it.tip)
            }
            is LoadState.Error -> {
                if (!this.isFinishing) {
                    toast(it.msg)
                }
            }
            is LoadState.Finish -> {
                dismissLoading()
            }
        }
    }

    private fun checkAndOpenGPS() {
        if (checkGPSOpen()) {
            checkPermission()
        } else {
            val popup = LocationPopup(this)
            popup.setListener {
                openGPS()
            }
            popup.mTitle = "打开位置服务"
            popup.mMessage = "请前往设置中打开位置服务"
            popup.mConfirmTest = "确定"
            popup.mCancelTest = "取消"
            XPopup.Builder(this)
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(popup)
                .show()
        }
    }

    private fun checkPermission() {
        if (checkGPSPermission()) {
            checkGetLocation()
        } else {
            val popup = LocationPopup(this)
            popup.setListener {
                PermissionUtils.permission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).callback(object : SimpleCallback {
                    override fun onGranted() {
                        checkGetLocation()
                    }
                    override fun onDenied() {
//                        PermissionUtils.launchAppDetailsSettings()
                    }
                }).request()
            }
            XPopup.Builder(this)
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(popup)
                .show()
        }
    }

    /**
     * 检查并获取位置
     */
    private fun checkGetLocation() {
        if (checkGPSOpen()) {
            showLoading(true, "定位中……")
            fromLocation = true
            mainViewModel.getLocation()
        } else {
            val popup = LocationPopup(this)
            popup.setListener {
                openGPS()
            }
            popup.mTitle = "打开位置服务"
            popup.mMessage = "请前往设置中打开位置服务"
            popup.mConfirmTest = "确定"
            popup.mCancelTest = "取消"
            XPopup.Builder(this)
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(popup)
                .show()
        }
    }

    /**
     * 启动GPS
     */
    private fun openGPS() {
        requestedGPS = true
        val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.add(PermissionFragment.newInstance(), "permission_fragment")
        beginTransaction.commitAllowingStateLoss()
    }

    /**
     * 展示热门城市
     */
    private fun showTopCity(locations: List<CityEntity>) {
        topCities.clear()
        topCities.addAll(locations)
        topCityAdapter?.notifyDataSetChanged()
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

    private fun configSearchLayout() {

        searchAdapter = SearchAdapter(
            this@AddCityActivity,
            searchCities,
            mBinding.etSearch.text.toString()
        ) {
            KeyboardUtils.hideSoftInput(this)
            showLoading()
            viewModel.addCity(it)
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

    private fun changedSearchType(isSearch: Boolean) {
        lifecycleScope.launch {
            if (isSearch) {
                mBinding.backButton.visibility = View.GONE
                mBinding.locationLayout.visibility = View.GONE
                mBinding.cancelBtn.visibility = View.VISIBLE
                mBinding.searchLayout.visibility = View.VISIBLE
                mBinding.privateStationBar.setBackgroundColor(Color.WHITE)
                mBinding.etSearch.visibility = View.VISIBLE
                mBinding.etSearchTemp.visibility = View.GONE
                mBinding.etSearch.showSoftInput(this@AddCityActivity.context)
            } else {
                KeyboardUtils.hideSoftInput(this@AddCityActivity)
                mBinding.backButton.visibility = View.VISIBLE
                mBinding.locationLayout.visibility = View.VISIBLE
                mBinding.cancelBtn.visibility = View.GONE
                mBinding.searchLayout.visibility = View.GONE
                mBinding.privateStationBar.setBackgroundColor(Color.TRANSPARENT)
                mBinding.etSearch.clearComposingText()
                mBinding.etSearch.visibility = View.GONE
                mBinding.etSearchTemp.visibility = View.VISIBLE
            }
        }
    }
}