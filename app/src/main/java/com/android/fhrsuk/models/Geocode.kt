package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class Geocode (

    @SerializedName("longitude") val longitude : Double,
    @SerializedName("latitude") val latitude : Double
)