package com.android.fhrsuk.models

import com.squareup.moshi.Json

data class Establishments(

    @Json(name = "FHRSID") val fHRSID: Int,
    @Json(name = "LocalAuthorityBusinessID") val localAuthorityBusinessID: String?,
    @Json(name = "BusinessName") val businessName: String,
    @Json(name = "BusinessType") val businessType: String,
    @Json(name = "BusinessTypeID") val businessTypeID: Int?,
    @Json(name = "AddressLine1") val addressLine1: String?,
    @Json(name = "AddressLine2") val addressLine2: String?,
    @Json(name = "AddressLine3") val addressLine3: String?,
    @Json(name = "AddressLine4") val addressLine4: String?,
    @Json(name = "PostCode") val postCode: String,
    @Json(name = "Phone") val phone: String?,
    @Json(name = "RatingValue") val ratingValue: String,
    @Json(name = "RatingKey") val ratingKey: String?,
    @Json(name = "RatingDate") val ratingDate: String,
    @Json(name = "LocalAuthorityCode") val localAuthorityCode: Int?,
    @Json(name = "LocalAuthorityName") val localAuthorityName: String?,
    @Json(name = "LocalAuthorityWebSite") val localAuthorityWebSite: String?,
    @Json(name = "LocalAuthorityEmailAddress") val localAuthorityEmailAddress: String?,
    @Json(name = "scores") val scores: Scores,
    @Json(name = "SchemeType") val schemeType: String?,
    @Json(name = "geocode") val geocode: Geocode,
    @Json(name = "RightToReply") val rightToReply: String?,
    @Json(name = "Distance") val distance: Double?,
    @Json(name = "NewRatingPending") val newRatingPending: Boolean?,
    @Json(name = "meta") val meta: Meta,
    @Json(name = "links") val links: List<Links>,
    @Transient var isExpanded: Boolean = false
)