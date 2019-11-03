package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class FHRSEstablishment(

    @SerializedName("Header") val header: Header,
    @SerializedName("EstablishmentCollection") val establishmentCollection: EstablishmentCollection
)