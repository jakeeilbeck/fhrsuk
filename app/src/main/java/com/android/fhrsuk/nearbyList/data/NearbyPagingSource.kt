package com.android.fhrsuk.nearbyList.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.api.RetrofitService
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 1

class NearbyPagingSource(
    private val retrofitService: RetrofitService,
    private var longitude: String,
    private var latitude: String) : PagingSource<Int, Establishments>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Establishments> {
        val position = params.key ?: STARTING_PAGE_INDEX

        return try {
            val response = retrofitService.getNearbyEstablishments(
                longitude,
                latitude,
                "distance",
                position,
                params.loadSize
            )
            val repos = response.establishments
            LoadResult.Page(
                data = repos,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (repos.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Establishments>): Int? {
        return state.anchorPosition
    }
}