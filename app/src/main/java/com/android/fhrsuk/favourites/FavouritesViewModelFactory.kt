package com.android.fhrsuk.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fhrsuk.favourites.data.FavouritesDao
import kotlinx.coroutines.ExperimentalCoroutinesApi

class FavouritesViewModelFactory (private val favouritesDatabase: FavouritesDao) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavouritesViewModel(favouritesDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}