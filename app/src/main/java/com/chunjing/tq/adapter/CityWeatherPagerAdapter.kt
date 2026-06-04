package com.chunjing.tq.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chunjing.tq.ui.fragment.WeatherFragment

/**
 * 按 cityId 稳定标识 ViewPager 页，增删城市时尽量 [notifyItemInserted]/[notifyItemRemoved]，
 * 避免每次全量重建 [WeatherFragment]。
 */
class CityWeatherPagerAdapter(
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {

    private val cityIds = mutableListOf<String>()

    fun cityIds(): List<String> = cityIds.toList()

    override fun getItemCount(): Int = cityIds.size

    override fun createFragment(position: Int): Fragment {
        return WeatherFragment.newInstance(cityIds[position])
    }

    override fun getItemId(position: Int): Long = stableItemId(cityIds[position])

    override fun containsItem(itemId: Long): Boolean {
        return cityIds.any { stableItemId(it) == itemId }
    }

    /**
     * @return true 表示已增量更新；false 表示已全量 [notifyDataSetChanged]
     */
    fun applyCityIds(newIds: List<String>): Boolean {
        val old = cityIds.toList()
        if (old == newIds) {
            return true
        }
        if (old.isEmpty()) {
            cityIds.addAll(newIds)
            notifyDataSetChanged()
            return false
        }
        // 仅在末尾追加（常见：新增城市）
        if (newIds.size > old.size && old == newIds.take(old.size)) {
            for (i in old.size until newIds.size) {
                cityIds.add(newIds[i])
                notifyItemInserted(i)
            }
            return true
        }
        // 仅删除一页
        if (old.size == newIds.size + 1) {
            val removedAt = old.indexOfFirst { it !in newIds }
            if (removedAt >= 0 && old.filter { it in newIds } == newIds) {
                cityIds.removeAt(removedAt)
                notifyItemRemoved(removedAt)
                return true
            }
        }
        // 同集合仅顺序变化
        if (old.toSet() == newIds.toSet()) {
            cityIds.clear()
            cityIds.addAll(newIds)
            notifyDataSetChanged()
            return false
        }
        cityIds.clear()
        cityIds.addAll(newIds)
        notifyDataSetChanged()
        return false
    }

    private fun stableItemId(cityId: String): Long = cityId.hashCode().toLong() and 0xFFFFFFFFL
}
