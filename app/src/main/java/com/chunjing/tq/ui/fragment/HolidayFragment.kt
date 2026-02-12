package com.chunjing.tq.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.adapter.HolidayAdapter
import com.chunjing.tq.calendarVM
import com.chunjing.tq.databinding.FragmentHolidayBinding
import com.chunjing.tq.ui.activity.vm.CalendarViewModel
import com.chunjing.tq.ui.base.BaseViewModel
import com.chunjing.tq.ui.base.BaseVmFragment
import com.goodtech.weatherlib.view.SpaceItemDecoration

@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class HolidayFragment : BaseVmFragment<FragmentHolidayBinding, BaseViewModel>() {

    private var mAdapter: HolidayAdapter? = null

    override fun bindView() = FragmentHolidayBinding.inflate(layoutInflater)

    override fun initView(view: View?) {
        mAdapter = HolidayAdapter(requireContext(), arrayListOf())
        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val spacesValue = HashMap<String, Int>()
        spacesValue[SpaceItemDecoration.BOTTOM_SPACE] = SizeUtils.dp2px(10f)
        mBinding.recyclerView.addItemDecoration(SpaceItemDecoration(spacesValue))
    }

    override fun initEvent() {
        calendarVM.mHolidayList.observe(this) {
            if (mAdapter != null) {
                mAdapter!!.data = it
                mAdapter!!.notifyDataSetChanged()
            }
            mBinding.tvYear.text = "${calendarVM.mHolidayYear}假期"
        }
    }

    override fun loadData() {

    }

    override fun onResume() {
        super.onResume()
        if (mAdapter != null) {
            if (calendarVM.mHolidayList.value != null) {
                mAdapter!!.data = calendarVM.mHolidayList.value!!
                mBinding.emptyView.visibility = View.GONE
            } else {
                mAdapter!!.data = arrayListOf()
                mBinding.emptyView.visibility = View.VISIBLE
            }
            mAdapter!!.notifyDataSetChanged()
        }
        mBinding.tvYear.text = "${calendarVM.mHolidayYear}假期"
    }
}