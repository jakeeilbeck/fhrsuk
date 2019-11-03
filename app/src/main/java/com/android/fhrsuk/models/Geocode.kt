package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class Geocode(

    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("Latitude") val latitude: Double
)