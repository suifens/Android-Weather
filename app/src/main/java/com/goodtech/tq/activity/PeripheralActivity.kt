package com.goodtech.tq.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.gengee.insaitlib.ui.base.BaseVmActivity
import com.goodtech.tq.adapter.PeripheralAdapter
import com.goodtech.tq.databinding.ActivityPeripheralBinding
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.viewmodel.PeripheralViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PeripheralActivity : BaseVmActivity<ActivityPeripheralBinding, PeripheralViewModel>() {

    companion object {
        fun startActivity(context: Context, cities: ArrayList<CityMode>) {
            val intent = Intent(context, PeripheralActivity::class.java)
            intent.putExtra("cities", cities)
            context.startActivity(intent)
        }
    }

    /**
     * Kotlin / R8 下父类泛型可能无法通过反射解析，显式提供 ViewModel 类型避免 ClassCastException。
     */
    @Suppress("UNCHECKED_CAST")
    override fun getViewModelClass(): Class<PeripheralViewModel> {
        return PeripheralViewModel::class.java as Class<PeripheralViewModel>
    }

    private var mCityList: ArrayList<CityMode>? = null
    private var mAdapter: PeripheralAdapter? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun prepareData(intent: Intent?) {
        intent?.let {
            val array = it.getSerializableExtra("cities") as ArrayList<CityMode>?
            if (array != null) {
                mCityList = array.toCollection(ArrayList())
            }
        }
    }

    override fun bindView() = ActivityPeripheralBinding.inflate(layoutInflater)

    override fun initView() {

        configStationBar(mBinding.privateStationBar)
        BarUtils.setStatusBarLightMode(this, true)

        mCityList?.let {
            mAdapter = PeripheralAdapter(it)
            mBinding.recyclerView.adapter = mAdapter
        }

        //  添加间距
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        mBinding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            @Deprecated("Deprecated in Java")
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                if (itemPosition == 0) {
                    outRect.top = SizeUtils.dp2px(20f)
                }
                outRect.top = SizeUtils.dp2px(20f)
            }
        })
    }

    override fun initEvent() {
        viewModel.weatherLiveData.observe(this) {
            mAdapter?.updateWeatherMap(it)
        }

        mBinding.backButton.setOnClickListener { finish() }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        val dayTime = TimeUtils.millis2String(System.currentTimeMillis(), "MM月dd日")
        mBinding.dayTv.text = "$dayTime | 今日"

        mCityList?.let {
            viewModel.setupCities(it)
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000L)
            viewModel.getWeatherData()
        }
    }
}