package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.databinding.ItemDayBinding
import com.goodtech.weatherlib.utils.DateUtil

class TravelDetailAdapter(
    var firstTime: Long = 0,
    var dayCount: Int = 0,
    val onChecked: (position: Int) -> Unit
) : RecyclerView.Adapter<TravelDetailAdapter.ViewHolder>() {

    private var selectedIndex: Int = 0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemDayBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.dayView.isSelected = position == selectedIndex
        val timestamp = firstTime + 24 * 60 * 60 * 1000 * position
        holder.binding.weekTv.text = DateUtil.getWeek(timestamp)
        holder.binding.timeTv.text = TimeUtils.millis2String(timestamp, "MM/dd")

        holder.binding.dayView.setOnClickListener {
            if (position != selectedIndex) {
                onChecked.invoke(position)
                val lastSelected = selectedIndex
                selectedIndex = position
                notifyItemChanged(lastSelected)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount() = dayCount

    class ViewHolder(val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}