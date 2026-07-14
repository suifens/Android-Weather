package com.chunjing.tq.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chunjing.tq.databinding.ItemCityManagerBinding
import com.chunjing.tq.db.entity.CityEntity
import java.util.Collections

class CityManagerAdapter(
    var onSort: ((List<CityEntity>) -> Unit)? = null
) : RecyclerView.Adapter<CityManagerAdapter.ViewHolder>(), IDragSort {

    private val items = ArrayList<CityEntity>()
    var listener: OnCityRemoveListener? = null

    fun submitList(list: List<CityEntity>) {
        val old = ArrayList(items)
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = old.size
            override fun getNewListSize() = list.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition].cityId == list[newItemPosition].cityId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val a = old[oldItemPosition]
                val b = list[newItemPosition]
                return a.cityId == b.cityId &&
                    a.cityName == b.cityName &&
                    a.sortOrder == b.sortOrder
            }
        })
        items.clear()
        items.addAll(list)
        diff.dispatchUpdatesTo(this)
    }

    fun removeAt(position: Int): CityEntity? {
        if (position !in items.indices) return null
        val removed = items.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    fun snapshot(): List<CityEntity> = ArrayList(items)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCityManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvItemCity.text = item.cityName

        holder.binding.tvDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                listener?.onCityRemove(pos)
            }
        }

        holder.binding.imgDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                listener?.onCityDeleteClick(pos)
            }
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(val binding: ItemCityManagerBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnCityRemoveListener {
        fun onCityRemove(pos: Int)
        fun onCityDeleteClick(pos: Int)
    }

    override fun move(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun dragFinish() {
        onSort?.invoke(snapshot())
    }
}
