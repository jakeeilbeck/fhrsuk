package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class Links (

    @SerializedName("rel") val rel : String,
    @SerializedName("href") val href : String
)