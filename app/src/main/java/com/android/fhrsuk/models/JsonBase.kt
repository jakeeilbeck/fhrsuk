package com.android.fhrsuk.models

import com.google.gson.annotations.SerializedName

data class JsonBase(

//	@SerializedName("?xml") val xml : Xml,
    @SerializedName("FHRSEstablishment") val fHRSEstablishment: FHRSEstablishment

)