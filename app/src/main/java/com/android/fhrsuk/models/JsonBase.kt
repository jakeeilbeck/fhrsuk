package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class JsonBase(

    @SerializedName("establishments") val establishments : List<Establishments>,
    @SerializedName("meta") val meta : Meta,
    @SerializedName("links") val links : List<Links>

)