package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class Establishments (

    @SerializedName("FHRSID") val fHRSID : Int,
    @SerializedName("LocalAuthorityBusinessID") val localAuthorityBusinessID : String,
    @SerializedName("BusinessName") val businessName : String,
    @SerializedName("BusinessType") val businessType : String,
    @SerializedName("BusinessTypeID") val businessTypeID : Int,
    @SerializedName("AddressLine1") val addressLine1 : String,
    @SerializedName("AddressLine2") val addressLine2 : String,
    @SerializedName("AddressLine3") val addressLine3 : String,
    @SerializedName("AddressLine4") val addressLine4 : String,
    @SerializedName("PostCode") val postCode : String,
    @SerializedName("Phone") val phone : String,
    @SerializedName("RatingValue") val ratingValue : String,
    @SerializedName("RatingKey") val ratingKey : String,
    @SerializedName("RatingDate") val ratingDate : String,
    @SerializedName("LocalAuthorityCode") val localAuthorityCode : Int,
    @SerializedName("LocalAuthorityName") val localAuthorityName : String,
    @SerializedName("LocalAuthorityWebSite") val localAuthorityWebSite : String,
    @SerializedName("LocalAuthorityEmailAddress") val localAuthorityEmailAddress : String,
    @SerializedName("scores") val scores : Scores,
    @SerializedName("SchemeType") val schemeType : String,
    @SerializedName("geocode") val geocode : Geocode,
    @SerializedName("RightToReply") val rightToReply : String,
    @SerializedName("Distance") val distance : Double,
    @SerializedName("NewRatingPending") val newRatingPending : Boolean,
    @SerializedName("meta") val meta : Meta,
    @SerializedName("links") val links : List<Links>,
    var isExpanded: Boolean
)