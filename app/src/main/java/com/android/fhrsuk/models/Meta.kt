package com.android.fhrsuk.models

import com.squareup.moshi.Json

data class Meta(

    @Json(name = "dataSource") val dataSource: String?,
    @Json(name = "extractDate") val extractDate: String,
    @Json(name = "itemCount") val itemCount: Int,
    @Json(name = "returncode") val returncode: String?,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "pageSize") val pageSize: Int,
    @Json(name = "pageNumber") val pageNumber: Int
)