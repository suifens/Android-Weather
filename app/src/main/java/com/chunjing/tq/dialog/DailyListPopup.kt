package com.chunjing.tq.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.adapter.WeatherListAdapter
import com.chunjing.tq.bean.Daily
import com.goodtech.weatherlib.view.GridSpaceItemDecoration
import com.lxj.xpopup.core.BottomPopupView

/**
 * com.chunjing.tq.dialog
 * 15天预报陈列图
 */
@SuppressLint("ViewConstructor")
class DailyListPopup(context: Context) : BottomPopupView(context) {
    
    private var mAdapter: WeatherListAdapter? = null
    private val mData by lazy { ArrayList<Daily>() }

    override fun getImplLayoutId(): Int = R.layout.dialog_daily_list

    override fun onCreate() {
        super.onCreate()

        findViewById<ImageButton>(R.id.closeBtn).setOnClickListener {
            dismiss()
        }
        mAdapter = WeatherListAdapter(mData)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = mAdapter

        //  添加间距
        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(
            GridSpaceItemDecoration(
                2,
                SizeUtils.dp2px(20f),
                (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(150f) * 2)/3
            )
        )
    }

    fun setupData(data: List<Daily>) {
        mData.clear()
        mData.addAll(data)
    }

    @SuppressLint("SetTextI18n")
    override fun onShow() {
        super.onShow()
    }
}
