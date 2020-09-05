package com.android.fhrsuk

import androidx.lifecycle.ViewModelProvider
import com.android.fhrsuk.nearbyList.NearbyViewModelFactory
import com.android.fhrsuk.nearbyList.data.NearbyRepository
import com.android.fhrsuk.api.RetrofitService
import com.android.fhrsuk.search.data.SearchRepository
import com.android.fhrsuk.search.SearchViewModelFactory

object Injection {

    //basic dependency injection for the ViewModels and the Repositories

    private fun provideNearbyRepository(): NearbyRepository {
        return NearbyRepository(RetrofitService.create())
    }

    fun provideNearbyViewModelFactory(): ViewModelProvider.Factory {
        return NearbyViewModelFactory(provideNearbyRepository())
    }

    private fun provideSearchRepository(): SearchRepository {
        return SearchRepository(RetrofitService.create())
    }

    fun provideSearchViewModelFactory(): ViewModelProvider.Factory {
        return SearchViewModelFactory(provideSearchRepository())
    }
}