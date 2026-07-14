package com.chunjing.tq.db

/**
 * 城市列表/Tab 相关的纯逻辑，便于单测，不含 Android 依赖。
 */
object CityListLogic {

    data class CityTabSnapshot(
        val cityId: String,
        val sortOrder: Int,
        val latitude: String,
        val longitude: String,
        val cityName: String,
        val mergerName: String,
    )

    fun isEquivalentForTabs(
        old: List<CityTabSnapshot>?,
        new: List<CityTabSnapshot>
    ): Boolean {
        if (old == null || old.size != new.size) return false
        for (i in new.indices) {
            val a = old[i]
            val b = new[i]
            if (a.cityId != b.cityId) return false
            if (a.sortOrder != b.sortOrder) return false
            if (a.latitude != b.latitude || a.longitude != b.longitude) return false
            if (a.cityName != b.cityName || a.mergerName != b.mergerName) return false
        }
        return true
    }

    /** 当前 Tab ±1 的 cityId 集合 */
    fun refreshWindowIds(cityIds: List<String>, centerIndex: Int): Set<String> {
        if (cityIds.isEmpty()) return emptySet()
        val window = LinkedHashSet<String>()
        for (offset in -1..1) {
            val idx = centerIndex + offset
            if (idx in cityIds.indices) {
                window.add(cityIds[idx])
            }
        }
        return window
    }

    /**
     * 为非定位城按列表下标写入 sortOrder（定位城固定 0）。
     * @return cityId → sortOrder
     */
    fun sortOrdersFor(
        cities: List<Pair<String, Boolean>>,
        locationId: String = "100000"
    ): List<Pair<String, Int>> {
        return cities.mapIndexed { index, (cityId, isLocal) ->
            val order = when {
                isLocal || cityId == locationId -> 0
                else -> index + 1
            }
            cityId to order
        }
    }

    /** 新增用户城时的 sortOrder：已存在则保留，否则 max+1；定位城为 0 */
    fun assignSortOrder(
        cityId: String,
        isLocal: Boolean,
        existingOrder: Int?,
        maxSortOrder: Int,
        locationId: String = "100000"
    ): Int {
        if (isLocal || cityId == locationId) return 0
        if (existingOrder != null) return existingOrder
        return maxSortOrder + 1
    }
}
