package com.chunjing.tq.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chunjing.tq.ui.fragment.WeatherFragment

/**
 * 按 cityId 创建 [WeatherFragment]，配合 stable [getItemId] 避免预创建 Fragment 实例。
 */
class WeatherPagerAdapter(
    fragment: Fragment,
    cityIds: List<String> = emptyList(),
) : FragmentStateAdapter(fragment) {

    private var cityIds: List<String> = cityIds

    override fun getItemCount(): Int = cityIds.size

    override fun createFragment(position: Int): Fragment {
        return WeatherFragment.newInstance(cityIds[position])
    }

    override fun getItemId(position: Int): Long {
        return cityIds[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return cityIds.any { it.hashCode().toLong() == itemId }
    }

    fun submitCityIds(newIds: List<String>) {
        cityIds = newIds
        notifyDataSetChanged()
    }

    fun appendCity(cityId: String) {
        cityIds = cityIds + cityId
        notifyItemInserted(cityIds.size - 1)
    }

    fun currentCityIds(): List<String> = cityIds
}
