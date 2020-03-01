package com.android.fhrsuk.models

import com.squareup.moshi.Json

data class Scores(

    @Json(name = "Hygiene") val hygiene: Int?,
    @Json(name = "Structural") val structural: Int?,
    @Json(name = "ConfidenceInManagement") val confidenceInManagement: Int?
)