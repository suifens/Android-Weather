package com.wkq.location

import android.location.Location

data class LocationResult(
    val success: Boolean,
    val location: Location?,
    val msg: String
)
