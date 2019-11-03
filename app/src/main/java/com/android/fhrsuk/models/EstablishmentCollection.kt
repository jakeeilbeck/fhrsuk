package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class EstablishmentCollection(

//	@SerializedName("@xmlns:xsd") val xmlnsXsd : String,
//	@SerializedName("@xmlns:xsi") val xmlnsXsi : String,
    @SerializedName("EstablishmentDetail")
    var establishmentDetail: List<EstablishmentDetail>
)


