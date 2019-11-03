package com.android.fhrsuk.network

import com.android.fhrsuk.models.JsonBase
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface EstablishmentApi {

    @GET("enhanced-search/en-GB/%5E/%5E/DISTANCE/0/%5E/{longitude}/{latitude}/{page}/{pagesize}/{json}")
    fun getNearby(
        @Path("longitude") longitude: String,
        @Path("latitude") latitude: String,
        @Path("page") page: Int,
        @Path("pagesize") size: Int,
        @Path("json") responseType: String
    ): Call<JsonBase>

    @GET("search/{name}/{location}/{page}/{pagesize}/{json}")
    fun getSearch(
        @Path("name") name: String,
        @Path("location") location: String,
        @Path("page") page: Int,
        @Path("pagesize") size: Int,
        @Path("json") type: String
    ): Call<JsonBase>
}