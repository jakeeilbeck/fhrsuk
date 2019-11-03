package com.android.fhrsuk.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://ratings.food.gov.uk/"

object RetrofitService {

    var gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(SingletonListTypeAdapter.FACTORY)
        .create()

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}