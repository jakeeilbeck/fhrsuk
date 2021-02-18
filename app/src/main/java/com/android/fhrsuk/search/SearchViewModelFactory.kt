package com.android.fhrsuk.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fhrsuk.favourites.data.FavouritesDao
import com.android.fhrsuk.search.data.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SearchViewModelFactory(private val repository: SearchRepository,
                             private val favouritesDatabase: FavouritesDao
) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository, favouritesDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}