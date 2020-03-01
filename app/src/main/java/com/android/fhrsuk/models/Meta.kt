package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class Meta (

    @SerializedName("dataSource") val dataSource : String,
    @SerializedName("extractDate") val extractDate : String,
    @SerializedName("itemCount") val itemCount : Int,
    @SerializedName("returncode") val returncode : String,
    @SerializedName("totalCount") val totalCount : Int,
    @SerializedName("totalPages") val totalPages : Int,
    @SerializedName("pageSize") val pageSize : Int,
    @SerializedName("pageNumber") val pageNumber : Int
)