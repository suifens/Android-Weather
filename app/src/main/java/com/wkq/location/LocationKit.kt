package com.wkq.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationKit : LocationListener {

    private var locationManager: LocationManager? = null
    private lateinit var appContext: Context
    private var currentConfig: LocationConfig = LocationConfig()
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    var callback: ((LocationResult) -> Unit)? = null

    private val lock = Any()
    private var lastLocation: Location? = null
    private var singleLocationReceived = false

    fun init(context: Context, config: LocationConfig = LocationConfig()) {
        appContext = context.applicationContext
        locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        currentConfig = config
    }

    fun startLocation(listener: (LocationResult) -> Unit) {
        if (locationManager == null) {
            listener(LocationResult(false, createDefaultLocation(), "LocationManager 初始化失败"))
            return
        }
        if (!hasPermission()) {
            listener(LocationResult(false, createDefaultLocation(), "缺少定位权限"))
            return
        }

        val providers = locationManager?.getProviders(true).orEmpty()
        if (providers.isEmpty()) {
            listener(LocationResult(false, createDefaultLocation(), "未启用定位服务，使用默认值"))
            return
        }

        callback = listener
        singleLocationReceived = false
        lastLocation = null
        retryCount = 0

        val type = currentConfig.getLocationType()
        val minTimeMs = currentConfig.getMinTimeMs()
        val minDistanceM = currentConfig.getMinDistanceM()

        when (type) {
            LocationType.FAST -> {
                val last = getValidLastKnownLocation() ?: createDefaultLocation()
                processLocation(last, force = true)
                requestAllProviders(minTimeMs, minDistanceM)
            }
            LocationType.FUSION, LocationType.SINGLE -> requestAllProviders(minTimeMs, minDistanceM)
            LocationType.LOCATION_NET -> requestProvider(LocationManager.NETWORK_PROVIDER, minTimeMs, minDistanceM)
            LocationType.LOCATION_GPS -> requestProvider(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM)
        }

        timeoutRunnable = Runnable {
            synchronized(lock) {
                if (!singleLocationReceived) {
                    val cached = getValidLastKnownLocation()
                    if (cached != null) {
                        processLocation(cached, force = true)
                    } else {
                        processLocation(createDefaultLocation(), force = true)
                        retryIfNoLocation(minTimeMs, minDistanceM)
                    }
                }
            }
        }
        handler.postDelayed(timeoutRunnable!!, currentConfig.getTimeout())
    }

    fun stopLocation() {
        locationManager?.removeUpdates(this)
        timeoutRunnable?.let { handler.removeCallbacks(it) }
    }

    @SuppressLint("MissingPermission")
    private fun requestProvider(provider: String, time: Long, distance: Float) {
        locationManager?.let {
            if (it.isProviderEnabled(provider)) {
                it.requestLocationUpdates(provider, time, distance, this)
            }
        }
    }

    private fun requestAllProviders(minTimeMs: Long, minDistanceM: Float) {
        val providers = locationManager?.getProviders(true).orEmpty()
        val finalProviders = if (providers.isEmpty()) {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        } else providers
        finalProviders.forEach { provider -> requestProvider(provider, minTimeMs, minDistanceM) }
    }

    @SuppressLint("MissingPermission")
    private fun getValidLastKnownLocation(): Location? {
        if (!hasPermission()) return null
        val now = System.currentTimeMillis()
        var best: Location? = null
        locationManager?.getProviders(true)?.forEach { provider ->
            val loc = locationManager?.getLastKnownLocation(provider) ?: return@forEach
            if (now - loc.time > 60_000) return@forEach
            if (best == null || loc.accuracy < best!!.accuracy) best = loc
        }
        return best
    }

    private var retryCount = 0
    private val maxRetry = 2

    private fun retryIfNoLocation(minTimeMs: Long, minDistanceM: Float) {
        if (retryCount >= maxRetry) return
        retryCount++
        handler.postDelayed({
            if (!singleLocationReceived) {
                requestAllProviders(minTimeMs, minDistanceM)
            }
        }, 2000)
    }

    private fun hasPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createDefaultLocation(): Location {
        val loc = Location("default")
        loc.latitude = currentConfig.getDefaultLatitude()
        loc.longitude = currentConfig.getDefaultLongitude()
        return loc
    }

    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toFloat()
    }

    private fun isValidLocation(last: Location?, current: Location): Boolean {
        last ?: return true
        val distance = distanceBetween(last.latitude, last.longitude, current.latitude, current.longitude)
        if (distance < currentConfig.getFilterMin()) return false
        if (distance > currentConfig.getFilterMax()) return false
        return true
    }

    fun processLocation(location: Location, force: Boolean = false) {
        synchronized(lock) {
            if (singleLocationReceived) return
            val type = currentConfig.getLocationType()

            if (!force && currentConfig.getFilter() && !isValidLocation(lastLocation, location)) return
            if (!force && type == LocationType.FUSION && lastLocation != null &&
                location.accuracy >= lastLocation!!.accuracy
            ) return
            if (!force && lastLocation != null && lastLocation!!.latitude == location.latitude &&
                lastLocation!!.longitude == location.longitude
            ) return

            lastLocation = location
            callback?.invoke(LocationResult(true, location, "定位成功"))

            if (type == LocationType.SINGLE) {
                singleLocationReceived = true
                stopLocation()
            }

            timeoutRunnable?.let { handler.removeCallbacks(it) }

            if (currentConfig.isUseBroadcast()) {
                val intent = Intent(LocationBroadcast.ACTION_LOCATION_UPDATE)
                intent.putExtra(LocationBroadcast.EXTRA_LOCATION, location)
                appContext.sendBroadcast(intent)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        processLocation(location)
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
        if (locations.isNotEmpty()) processLocation(locations[0])
    }
}
