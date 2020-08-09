package com.android.fhrsuk.nearbyList

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.network.NearbyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
class NearbyViewModel(private val repository: NearbyRepository) : ViewModel() {

    private lateinit var longitude: String
    private lateinit var latitude: String

    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Establishments>>? = null

    fun searchRepo(): Flow<PagingData<Establishments>> {
        val lastResult = currentSearchResult
        if (longitude + latitude == currentQueryValue && lastResult != null) {
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
}