package com.chunjing.tq.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import coil.transform.RoundedCornersTransformation
import com.chunjing.tq.MyApp
import com.chunjing.tq.databinding.ItemPopularCityBinding
import com.chunjing.tq.databinding.ItemPopularCityHeaderBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.utils.PopularUtil
import com.goodtech.weatherlib.BaseApp

class PopularCityAdapter(val mData: List<CityEntity>, val onChecked: (CityEntity) -> Unit) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) return 0 else 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        if (viewType == 0) {
            return HeaderViewHolder(
                ItemPopularCityHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        return MyViewHolder(
            ItemPopularCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]

        if (getItemViewType(position) == 0) {
            (holder as HeaderViewHolder).binding.tvCityName.text = item.shortName
            holder.binding.tvCityPy.text = item.pinyin
            val imgRes = PopularUtil.getCityImgRes(BaseApp.context, item.shortName)
            if (imgRes != 0) {
                holder.binding.cityImgView.setImageResource(imgRes)
            }
        } else {
            (holder as MyViewHolder).binding.tvCityName.text = item.shortName
            holder.binding.tvCityPy.text = item.pinyin
            val imgRes = PopularUtil.getCityImgRes(BaseApp.context, item.shortName)
            if (imgRes != 0) {
                holder.binding.cityImgView.setImageResource(imgRes)
            }
        }
        holder.itemView.setOnClickListener {
            onChecked(item)
        }
    }

    override fun getItemCount() = mData.size

    class MyViewHolder(val binding: ItemPopularCityBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    class HeaderViewHolder(val binding: ItemPopularCityHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}