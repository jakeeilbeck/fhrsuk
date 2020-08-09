package com.android.fhrsuk.network

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.nearbyList.NearbyPagingSource
import kotlinx.coroutines.flow.Flow

class NearbyRepository(private val service: RetrofitService) {
    fun getEstablishmentsStream(longitude: String, latitude: String): Flow<PagingData<Establishments>> {
        return Pager(
            config = PagingConfig(pageSize = 50),
            pagingSourceFactory = {NearbyPagingSource(service, longitude, latitude)}
        ).flow
    }
}