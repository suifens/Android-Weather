package com.chunjing.tq.db

import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.goodtech.weatherlib.BaseApp
import com.mapzen.android.lost.api.LocationListener
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import java.util.Locale

/**
 * LOST 单次定位；Geocoder / cityCode 解析为 [location2CityEntity] / [resolveLocationCityCode]。
 */
class LocationRepository private constructor(
    private val appRepo: AppRepo = AppRepo.getInstance()
) {
    interface Callbacks {
        fun onStart()
        fun onLocation(location: Location)
        fun onError(message: String)
        fun onFinish()
    }

    private var lostApiClient: LostApiClient? = null
    private var locationListener: LocationListener? = null

    @Volatile
    private var locationRequestActive = false

    @Synchronized
    private fun tryBeginLocationRequest(): Boolean {
        if (locationRequestActive) return false
        locationRequestActive = true
        return true
    }

    @Synchronized
    fun endLocationRequest() {
        locationRequestActive = false
    }

    fun requestLocation(callbacks: Callbacks) {
        if (!tryBeginLocationRequest()) {
            return
        }
        callbacks.onStart()
        val client = lostApiClient ?: createLostApiClient(callbacks)
        if (client.isConnected()) {
            requestSingleUpdateFallback(client, callbacks)
            return
        }
        try {
            client.connect()
        } catch (_: SecurityException) {
            endLocationRequest()
            callbacks.onError("定位权限不足，请重试")
            callbacks.onFinish()
        } catch (_: Exception) {
            endLocationRequest()
            callbacks.onError("获取定位失败,请重试")
            callbacks.onFinish()
        }
    }

    private fun createLostApiClient(callbacks: Callbacks): LostApiClient {
        val client = LostApiClient.Builder(BaseApp.context)
            .addConnectionCallbacks(object : LostApiClient.ConnectionCallbacks {
                override fun onConnected() {
                    requestSingleUpdateFallback(lostApiClient ?: return, callbacks)
                }

                override fun onConnectionSuspended() {
                    endLocationRequest()
                    callbacks.onError("获取定位失败,请重试")
                    callbacks.onFinish()
                }
            })
            .build()
        lostApiClient = client
        return client
    }

    private fun requestSingleUpdateFallback(client: LostApiClient, callbacks: Callbacks) {
        val request = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000L)
            .setFastestInterval(1000L)

        clearLocationListener()
        locationListener = LocationListener { location ->
            clearLocationListener()
            if (location != null) {
                deliverLocation(location, callbacks)
            } else {
                endLocationRequest()
                callbacks.onError("获取定位失败,请重试")
                callbacks.onFinish()
            }
        }

        try {
            val lastKnown = LocationServices.FusedLocationApi.getLastLocation(client)
            val isRecentLocation = lastKnown != null &&
                (System.currentTimeMillis() - lastKnown.time) <= MAX_LAST_KNOWN_AGE_MS
            if (isRecentLocation) {
                deliverLocation(lastKnown, callbacks)
                return
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                client,
                request,
                locationListener ?: return
            )
        } catch (_: SecurityException) {
            clearLocationListener()
            endLocationRequest()
            callbacks.onError("定位权限不足，请重试")
            callbacks.onFinish()
        } catch (_: Exception) {
            clearLocationListener()
            endLocationRequest()
            callbacks.onError("获取定位失败,请重试")
            callbacks.onFinish()
        }
    }

    private fun deliverLocation(location: Location, callbacks: Callbacks) {
        clearLocationListener()
        val client = lostApiClient
        if (client != null && client.isConnected()) {
            try {
                client.disconnect()
            } catch (_: Exception) {
            }
        }
        callbacks.onLocation(location)
        // endLocationRequest / onFinish 由 ViewModel 在协程完成后调用
    }

    private fun clearLocationListener() {
        val client = lostApiClient
        val listener = locationListener
        if (client != null && client.isConnected() && listener != null) {
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(client, listener)
            } catch (_: Exception) {
            }
        }
        locationListener = null
    }

    fun location2CityEntity(location: Location): CityEntity {
        val cityEntity = CityEntity()
        var cityName = ""
        var district = ""
        var poiName = ""
        var cityCode = ""

        try {
            val geocoder = Geocoder(BaseApp.context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            cityName = address?.locality ?: address?.subAdminArea ?: ""
            district = address?.subLocality ?: address?.subAdminArea ?: ""
            poiName = address?.featureName ?: ""
            cityCode = address?.postalCode ?: ""
        } catch (_: Exception) {
        }

        val fallbackName = cityName.ifBlank { "当前位置" }
        cityEntity.cityId = LOCATION_ID
        cityEntity.cityName = fallbackName
        cityEntity.cityCode = cityCode
        cityEntity.shortName = fallbackName
        cityEntity.mergerName =
            listOf(district, poiName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { fallbackName }
        cityEntity.latitude = location.latitude.toString()
        cityEntity.longitude = location.longitude.toString()
        cityEntity.setLocal()
        Log.e("TAG", "location2CityEntity: city = $cityEntity")
        return cityEntity
    }

    suspend fun resolveLocationCityCode(city: CityEntity): String {
        val candidates = linkedSetOf(
            city.cityName,
            city.shortName,
            city.mergerName,
            city.mergerName.substringBefore(" ").trim(),
            city.cityName.removeSuffix("市").removeSuffix("县").removeSuffix("区").trim(),
            city.shortName.removeSuffix("市").removeSuffix("县").removeSuffix("区").trim()
        ).filter { it.isNotBlank() }

        for (keyword in candidates) {
            val match = appRepo.searchCity(keyword)
                .firstOrNull { it.cityCode.isNotBlank() && !it.isLocal() }
            if (match != null) {
                return match.cityCode
            }
        }
        return ""
    }

    companion object {
        private const val MAX_LAST_KNOWN_AGE_MS = 2 * 60 * 1000L

        @Volatile
        private var instance: LocationRepository? = null

        @JvmStatic
        fun getInstance(): LocationRepository =
            instance ?: synchronized(this) {
                instance ?: LocationRepository().also { instance = it }
            }
    }
}
