package com.android.fhrsuk.nearbyList

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.models.JsonBase
import com.android.fhrsuk.network.EstablishmentApi
import com.android.fhrsuk.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val PAGE_SIZE = 50
private const val SORT_OPTION_KEY = "distance"

//PagedKeyDataSource used to load data in pages
class NearbyRepository(
    var context: Context,
    private var longitude: String,
    private var latitude: String
) :
    PageKeyedDataSource<Int, Establishments>() {

    private var establishmentApi: EstablishmentApi = RetrofitService.getRetrofit()
        .create(EstablishmentApi::class.java)
    var data = MutableLiveData<JsonBase>()

    private var currentPage: Int = 1
    private var maxPages = 0

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Establishments>
    ) {

        val loadingState = NearbyLoadingState
        loadingState.setLoadingState(1)

        establishmentApi.getNearbyEstablishments(
            longitude, latitude, SORT_OPTION_KEY, currentPage, PAGE_SIZE
        )
            .enqueue(object : Callback<JsonBase> {

                override fun onResponse(call: Call<JsonBase>, response: Response<JsonBase>) {
                    if (response.body() != null) {
                        if (response.body()?.meta?.itemCount!! >= 1) {
                            callback.onResult(
                                response.body()!!.establishments,
                                null,
                                currentPage++
                            )
                            maxPages = response.body()!!.meta.totalPages + 1

                        } else {
                            //Successful response but 0 results
                            Toast.makeText(
                                context,
                                context.getString(com.android.fhrsuk.R.string.response_no_data_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    loadingState.setLoadingState(0)
                }

                override fun onFailure(call: Call<JsonBase>, t: Throwable) {
                    loadingState.setLoadingState(0)
                    Toast.makeText(
                        context,
                        context.getString(com.android.fhrsuk.R.string.response_no_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i("EstablishmentRepository", "OnFailure! $t")
                }
            })
    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, Establishments>
    ) {

        establishmentApi.getNearbyEstablishments(
            longitude, latitude, SORT_OPTION_KEY, currentPage, PAGE_SIZE
        )
            .enqueue(object : Callback<JsonBase> {

                override fun onResponse(call: Call<JsonBase>, response: Response<JsonBase>) {

                    if (response.body() != null && currentPage != maxPages) {
                        if (response.body()?.meta?.itemCount!! >= 1) {
                            val key =
                                (if (response.body()!!.meta.pageNumber != maxPages) params.key + 1 else null)?.toInt()
                            callback.onResult(
                                response.body()!!.establishments,
                                key
                            )

                        } else {
                            //Successful response but 0 results
                            Toast.makeText(
                                context,
                                context.getString(com.android.fhrsuk.R.string.response_no_data_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        currentPage++
                    }
                }

                override fun onFailure(call: Call<JsonBase>, t: Throwable) {
                    Toast.makeText(
                        context,
                        context.getString(com.android.fhrsuk.R.string.response_no_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i("EstablishmentRepository", "OnFailure! $t")
                }
            })
    }

    override fun loadBefore(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, Establishments>
    ) {

    }
}