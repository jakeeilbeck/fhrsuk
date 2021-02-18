package com.android.fhrsuk

import androidx.lifecycle.ViewModelProvider
import com.android.fhrsuk.nearbyList.NearbyViewModelFactory
import com.android.fhrsuk.nearbyList.data.NearbyRepository
import com.android.fhrsuk.api.RetrofitService
import com.android.fhrsuk.favourites.data.FavouritesDao
import com.android.fhrsuk.favourites.FavouritesViewModelFactory
import com.android.fhrsuk.search.data.SearchRepository
import com.android.fhrsuk.search.SearchViewModelFactory

object Injection {

    //basic dependency injection for the ViewModels and the Repositories

    private fun provideNearbyRepository(): NearbyRepository {
        return NearbyRepository(RetrofitService.create())
    }

    fun provideNearbyViewModelFactory(favouritesDao: FavouritesDao): ViewModelProvider.Factory {
        return NearbyViewModelFactory(provideNearbyRepository(), favouritesDao)
    }


    private fun provideSearchRepository(): SearchRepository {
        return SearchRepository(RetrofitService.create())
    }

    fun provideSearchViewModelFactory(favouritesDao: FavouritesDao): ViewModelProvider.Factory {
        return SearchViewModelFactory(provideSearchRepository(), favouritesDao)
    }

    fun provideFavouritesViewModelFactory(favouritesDao: FavouritesDao): ViewModelProvider.Factory {
        return FavouritesViewModelFactory(favouritesDao)
    }
}