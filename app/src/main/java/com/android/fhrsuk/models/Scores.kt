package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class Scores(

    @SerializedName("Hygiene") val hygiene: Int,
    @SerializedName("Structural") val structural: Int,
    @SerializedName("ConfidenceInManagement") val confidenceInManagement: Int
)