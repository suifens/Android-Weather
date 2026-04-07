package com.wkq.location

class LocationConfig {

    private var timeout: Long = 6000L
    private var minTimeMs: Long = 5000L
    private var minDistanceM: Float = 1f
    private var isFilter: Boolean = false
    private var filterMin = 5f
    private var filterMax: Float = 200f
    private var defaultLatitude: Double = 39.9042
    private var defaultLongitude: Double = 116.4074
    private var useBroadcast: Boolean = false
    private var locationType: LocationType = LocationType.FUSION

    fun setTimeout(value: Long) = apply { this.timeout = value }
    fun setMinTimeMs(value: Long) = apply { this.minTimeMs = value }
    fun setMinDistanceM(value: Float) = apply { this.minDistanceM = value }
    fun setFilter(filter: Boolean) = apply { this.isFilter = filter }
    fun setFilterMin(value: Float) = apply { this.filterMin = value }
    fun setDefaultLatitude(value: Double) = apply { this.defaultLatitude = value }
    fun setDefaultLongitude(value: Double) = apply { this.defaultLongitude = value }
    fun setFilterMax(value: Float) = apply { this.filterMax = value }
    fun setUseBroadcast(value: Boolean) = apply { this.useBroadcast = value }
    fun setLocationType(value: LocationType) = apply { this.locationType = value }

    fun getTimeout(): Long = timeout
    fun getMinTimeMs(): Long = minTimeMs
    fun getMinDistanceM(): Float = minDistanceM
    fun getFilter(): Boolean = isFilter
    fun getFilterMin(): Float = filterMin
    fun getDefaultLatitude(): Double = defaultLatitude
    fun getDefaultLongitude(): Double = defaultLongitude
    fun getFilterMax(): Float = filterMax
    fun isUseBroadcast(): Boolean = useBroadcast
    fun getLocationType(): LocationType = locationType
}
