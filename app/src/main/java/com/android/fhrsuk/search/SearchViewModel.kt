package com.android.fhrsuk.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.search.data.SearchRepository
import kotlinx.coroutines.flow.Flow

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {

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
}