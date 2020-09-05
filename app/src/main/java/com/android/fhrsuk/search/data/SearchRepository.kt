package com.android.fhrsuk.search.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.api.RetrofitService
import kotlinx.coroutines.flow.Flow

class SearchRepository(private val service: RetrofitService) {
    fun getEstablishmentsStream(name: String, location: String): Flow<PagingData<Establishments>> {
        return Pager(
            config = PagingConfig(pageSize = 50),
            pagingSourceFactory = {
                SearchPagingSource(
                    service,
                    name,
                    location
                )
            }
        ).flow
    }
}