package com.android.fhrsuk.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.fhrsuk.favourites.data.FavouritesDao
import com.android.fhrsuk.favourites.data.FavouritesTable
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.search.data.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: SearchRepository,
                      private val favouritesDatabase: FavouritesDao
) : ViewModel() {

    private lateinit var name: String
    private lateinit var location: String

    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Establishments>>? = null

    fun searchEstablishments(): Flow<PagingData<Establishments>> {
        val lastResult = currentSearchResult
        if ((name + location == currentQueryValue) && (lastResult != null)) {
            return lastResult
        }
        currentQueryValue = name + location

        val newResult: Flow<PagingData<Establishments>> =
            repository.getEstablishmentsStream(name, location).cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }

    fun setSearchTerms(name: String, location: String) {
        this.name = name
        this.location = location
    }

    fun getCurrentSearchResult(): Flow<PagingData<Establishments>>?{
        return currentSearchResult
    }

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
}