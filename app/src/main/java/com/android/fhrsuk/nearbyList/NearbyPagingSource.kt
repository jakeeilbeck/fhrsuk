package com.android.fhrsuk.nearbyList

import androidx.paging.PagingSource
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.network.RetrofitService
import retrofit2.HttpException
import java.io.IOException

class NearbyPagingSource(private val retrofitService: RetrofitService, private var longitude: String, private var latitude: String): PagingSource<Int, Establishments>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Establishments> {
        val position = params.key ?: 1
        return try {
            val response = retrofitService.getNearbyEstablishments(longitude, latitude, "distance", position, params.loadSize)
            val repos = response.establishments
            LoadResult.Page(
                data = repos,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (repos.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}