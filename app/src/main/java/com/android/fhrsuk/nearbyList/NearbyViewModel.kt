package com.android.fhrsuk.nearbyList

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.nearbyList.data.NearbyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NearbyViewModel(private val repository: NearbyRepository) : ViewModel() {

    private lateinit var longitude: String
    private lateinit var latitude: String

    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Establishments>>? = null

    fun searchEstablishments(): Flow<PagingData<Establishments>> {
        val lastResult = currentSearchResult
        if ((longitude + latitude == currentQueryValue) && (lastResult != null)) {
            return lastResult
        }
        currentQueryValue = longitude + latitude

        val newResult: Flow<PagingData<Establishments>> =
            repository.getEstablishmentsStream(longitude, latitude).cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }

    fun setLocation(location: Location) {
        this.longitude = location.longitude.toString()
        this.latitude = location.latitude.toString()
    }

    fun filterList(rating: String): Flow<PagingData<Establishments>>? {
        return if (rating == "clear") {
            currentSearchResult
        } else {
            currentSearchResult?.map { pagingData ->
                pagingData.filter { establishments ->
                    establishments.ratingValue == rating
                }
            }
        }
    }
}