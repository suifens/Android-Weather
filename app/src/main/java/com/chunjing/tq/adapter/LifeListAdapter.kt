package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.chunjing.tq.bean.LifeEntity
import com.chunjing.tq.bean.LifeItemBean
import com.chunjing.tq.databinding.ItemLifeBinding

@SuppressLint("NotifyDataSetChanged")
class LifeListAdapter(
    var mData: LifeEntity,
    private val isWhite: Boolean,
    private val onSelected: ((LifeItemBean) -> Unit)? = null
) : RecyclerView.Adapter<LifeListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemLifeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = when (position) {
            0 -> mData.chuanyi
            1 -> mData.daisan
            2 -> mData.xiche
            3 -> mData.diaoyu
            4 -> mData.yundong
            5 -> mData.ganmao
            6 -> mData.kongtiao
            7 -> mData.guomin
            else -> mData.chuanyi
        }

        if (item != null) {
            holder.binding.tvLife.text = "${mData.lifeTitleWith(item)} | ${item.v}"
            mData.lifeImageWith(item).let { holder.binding.lifeImgView.load(it) }
            if (isWhite) {
                holder.binding.tvLife.setTextColor(Color.WHITE)
            }
            holder.itemView.setOnClickListener {
                onSelected?.invoke(item)
            }
        }

    }

    fun updateData(data: LifeEntity) {
        mData = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return 8
    }

    class ViewHolder(val binding: ItemLifeBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}