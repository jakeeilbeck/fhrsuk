package com.android.fhrsuk.nearbyList

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.android.fhrsuk.favourites.data.FavouritesDao
import com.android.fhrsuk.favourites.data.FavouritesTable
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.nearbyList.data.NearbyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NearbyViewModel(private val repository: NearbyRepository,
                      private val favouritesDatabase: FavouritesDao
)
    : ViewModel() {

    private lateinit var longitude: String
    private lateinit var latitude: String

    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Establishments>>? = null
    private var currentFilter: String = "clear"

    private var isFirstSearch: Boolean = true

    private var filterExpanded: Boolean = false

    fun addRemoveFromFavourites(favourite: Establishments?){
        viewModelScope.launch {
            val newFavourite = FavouritesTable(
                favourite?.fHRSID,
                favourite?.businessName,
                favourite?.businessType,
                favourite?.addressLine1,
                favourite?.addressLine2,
                favourite?.postCode,
                favourite?.ratingValue,
                favourite?.ratingDate,
                favourite?.scores?.hygiene,
                favourite?.scores?.structural,
                favourite?.scores?.confidenceInManagement,
                System.currentTimeMillis())
            addRemove(newFavourite)
        }
    }

    private suspend fun addRemove(favourite: FavouritesTable){
        if(favouritesDatabase.checkExists(favourite.fHRSID) == 0){
            favouritesDatabase.insert(favourite)
        }else{
            favouritesDatabase.delete(favourite)
        }
    }

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

    fun getCurrentSearchResult(): Flow<PagingData<Establishments>>? {
        return currentSearchResult
    }

    fun setLocation(location: Location) {
        this.longitude = location.longitude.toString()
        this.latitude = location.latitude.toString()
    }

    fun filterList(rating: String): Flow<PagingData<Establishments>>? {
        currentFilter = rating
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

    fun getIsFirstSearch(): Boolean {
        return isFirstSearch
    }

    fun setIsFirstSearchFalse() {
        isFirstSearch = false
    }

    fun getCurrentFilter(): String {
        return currentFilter
    }

    fun getFilterVisibilityStatus(): Boolean{
        return filterExpanded
    }

    fun setFilterVisibilityStatus(status: Boolean){
        filterExpanded = status
    }
}