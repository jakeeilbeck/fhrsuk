package com.android.fhrsuk.nearbyList.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.fhrsuk.models.Establishments
import kotlinx.coroutines.flow.Flow
import com.android.fhrsuk.api.RetrofitService

class NearbyRepository(private val service: RetrofitService) {
    fun getEstablishmentsStream(longitude: String, latitude: String): Flow<PagingData<Establishments>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                initialLoadSize = 50),
            pagingSourceFactory = {NearbyPagingSource(service, longitude, latitude)
            }
        ).flow
    }
}