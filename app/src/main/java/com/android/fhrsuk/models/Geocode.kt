package com.android.fhrsuk.models

import com.squareup.moshi.Json

data class Geocode(

    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "latitude") val latitude: Double?
)