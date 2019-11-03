package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class Header(

    @SerializedName("#text") val text: String,
    @SerializedName("ExtractDate") val extractDate: String,
    @SerializedName("ItemCount") val itemCount: Int,
    @SerializedName("ReturnCode") val returnCode: String,
    @SerializedName("PageNumber") val pageNumber: Int,
    @SerializedName("PageSize") val pageSize: Int,
    @SerializedName("PageCount") val pageCount: Int
)