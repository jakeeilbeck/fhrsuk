package com.android.fhrsuk.api

import com.android.fhrsuk.models.JsonBase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitService {

    @Headers(
        "x-api-version: 2",
        "accept: application/json",
        "content-type: application/json"
    )
    @GET("Establishments?")
    suspend fun getNearbyEstablishments(
        @Query("longitude") longitude: String,
        @Query("latitude") latitude: String,
        @Query("sortOptionKey") sortOptionKey: String,
        @Query("pageNumber") page: Int,
        @Query("pageSize") size: Int
    ): JsonBase


    @Headers(
        "x-api-version: 2",
        "accept: application/json",
        "content-type: application/json"
    )
    @GET("Establishments?")
    suspend fun getSearchedEstablishments(
        @Query("name") name: String,
        @Query("address") location: String,
        @Query("sortOptionKey") sortOptionKey: String,
        @Query("pageNumber") page: Int,
        @Query("pageSize") size: Int
    ): JsonBase

    companion object{
        private const val BASE_URL = "https://api.ratings.food.gov.uk/"

        private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        fun create(): RetrofitService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RetrofitService::class.java)
        }
    }
}