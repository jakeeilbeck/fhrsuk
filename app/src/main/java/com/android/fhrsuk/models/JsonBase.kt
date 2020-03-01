package com.android.fhrsuk.models

import com.squareup.moshi.Json

data class JsonBase(

    @Json(name = "establishments") val establishments: List<Establishments>,
    @Json(name = "meta") val meta: Meta,
    @Json(name = "links") val links: List<Links>
)