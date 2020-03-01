package com.android.fhrsuk.models

import com.squareup.moshi.Json

data class Links(

    @Json(name = "rel") val rel: String?,
    @Json(name = "href") val href: String?
)