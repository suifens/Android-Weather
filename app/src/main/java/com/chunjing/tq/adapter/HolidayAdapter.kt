package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chunjing.tq.R
import com.chunjing.tq.bean.calendar.Holiday
import com.chunjing.tq.databinding.ItemHolidayBinding

class HolidayAdapter(val context: Context,
    var data: List<Holiday>
) : RecyclerView.Adapter<HolidayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemHolidayBinding.inflate(LayoutInflater.from(context), parent,false)
        )
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[holder.absoluteAdapterPosition]
        holder.binding.bgView.setBackgroundResource(getBgRes(position))
        holder.binding.tvHoliday.text = "${item.name} (${item.getDay() })"
        holder.binding.tvCount.text = "共${item.getHoliday()}天"
        holder.binding.tvHolidayTime.text = "假期：${item.getTimeSpan()}"
        holder.binding.tvPropose.text = item.desc
    }

    override fun getItemCount() = data.size

    private fun getBgRes(index: Int) : Int {
        return when (index) {
            1 -> R.drawable.gradient_holiday_2
            2 -> R.drawable.gradient_holiday_3
            3 -> R.drawable.gradient_holiday_4
            4 -> R.drawable.gradient_holiday_5
            5 -> R.drawable.gradient_holiday_6
            6 -> R.drawable.gradient_holiday_7
            else -> R.drawable.gradient_holiday_1
        }
    }

    class ViewHolder(val binding: ItemHolidayBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}