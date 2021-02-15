package com.android.fhrsuk.nearbyList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fhrsuk.favourites.FavouritesDao
import com.android.fhrsuk.nearbyList.data.NearbyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NearbyViewModelFactory(private val repository: NearbyRepository,
                            private val favouritesDatabase: FavouritesDao) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NearbyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NearbyViewModel(repository, favouritesDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}