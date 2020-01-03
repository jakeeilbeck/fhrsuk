package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class EstablishmentDetail(

    @SerializedName("FHRSID") val fHRSID: Int,
    @SerializedName("LocalAuthorityBusinessID") val localAuthorityBusinessID: String,
    @SerializedName("BusinessName") val businessName: String,
    @SerializedName("BusinessType") val businessType: String,
    @SerializedName("BusinessTypeID") val businessTypeID: Int,
    @SerializedName("AddressLine1") val addressLine1: String,
    @SerializedName("AddressLine2") val addressLine2: String,
    @SerializedName("AddressLine3") val addressLine3: String,
    @SerializedName("AddressLine4") val addressLine4: String,
    @SerializedName("PostCode") val postCode: String,
    @SerializedName("RatingValue") val ratingValue: String,
    @SerializedName("RatingKey") val ratingKey: String,
    @SerializedName("RightToReply") val rightToReply: String,
    @SerializedName("RatingDate") val ratingDate: String,
    @SerializedName("LocalAuthorityCode") val localAuthorityCode: Int,
    @SerializedName("LocalAuthorityName") val localAuthorityName: String,
    @SerializedName("LocalAuthorityWebSite") val localAuthorityWebSite: String,
    @SerializedName("LocalAuthorityEmailAddress") val localAuthorityEmailAddress: String,
    @SerializedName("com.android.fhrsuk.models.Scores") val scores: Scores,
    @SerializedName("SchemeType") val schemeType: String,
    @SerializedName("NewRatingPending") val newRatingPending: Boolean,
    @SerializedName("com.android.fhrsuk.models.Geocode") val geocode: Geocode,
    var isExpanded: Boolean
    //@SerializedName("Distance") val distance : Double?

)