package com.chunjing.tq.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chunjing.tq.R
import com.chunjing.tq.databinding.ItemSearchingBinding
import com.chunjing.tq.db.entity.CityEntity

/**
 * 最近搜索
 */
class SearchAdapter(
    private val mContext: Context,
    private val data: List<CityEntity>,
    private val searchText: String,
    private val onCityChecked: (CityEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ItemSearchingBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(
        myViewHolder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val viewHolder = myViewHolder as ViewHolder
        val item = data[position]
        val cityName = item.mergerName
        if (!TextUtils.isEmpty(cityName)) {
            viewHolder.binding.tvCity.text = cityName
            if (cityName.contains(searchText)) {
                val index = cityName.indexOf(searchText)
                //创建一个 SpannableString对象
                val sp = SpannableString(cityName)
                //设置高亮样式一
                sp.setSpan(
                    ForegroundColorSpan(mContext.resources.getColor(R.color.light_text_color)),
                    index,
                    index + searchText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                viewHolder.binding.tvCity.text = sp
            }
        }
        viewHolder.itemView.setOnClickListener { view: View? ->
            onCityChecked(data[position])
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    internal inner class ViewHolder(val binding: ItemSearchingBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}