package com.android.fhrsuk.network

import com.android.fhrsuk.models.JsonBase
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface EstablishmentApi {

    @Headers(
        "x-api-version: 2",
        "accept: application/json",
        "content-type: application/json"
    )
    @GET("Establishments?")
    fun getNearbyEstablishments(
        @Query("longitude") longitude: String,
        @Query("latitude") latitude: String,
        @Query("sortOptionKey") sortOptionKey: String,
        @Query("pageNumber") page: Int,
        @Query("pageSize") size: Int
    ): Call<JsonBase>


    @Headers(
        "x-api-version: 2",
        "accept: application/json",
        "content-type: application/json"
    )
    @GET("Establishments?")
    fun getSearchedEstablishments(
        @Query("name") name: String,
        @Query("address") location: String,
        @Query("sortOptionKey") sortOptionKey: String,
        @Query("pageNumber") page: Int,
        @Query("pageSize") size: Int
    ): Call<JsonBase>

}