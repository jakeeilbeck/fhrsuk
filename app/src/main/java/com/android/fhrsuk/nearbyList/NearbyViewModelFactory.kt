package com.android.fhrsuk.nearbyList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fhrsuk.network.NearbyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NearbyViewModelFactory(private val repository: NearbyRepository) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NearbyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NearbyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}