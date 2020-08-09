package com.android.fhrsuk

import androidx.lifecycle.ViewModelProvider
import com.android.fhrsuk.nearbyList.NearbyViewModelFactory
import com.android.fhrsuk.network.NearbyRepository
import com.android.fhrsuk.network.RetrofitService
import com.android.fhrsuk.network.SearchRepository
import com.android.fhrsuk.search.SearchViewModelFactory

object Injection {
    /**
     * Creates an instance of [GithubRepository] based on the [GithubService] and a
     * [GithubLocalCache]
     */
    private fun provideGithubRepository(): NearbyRepository {
        return NearbyRepository(RetrofitService.create())
    }

    /**
     * Provides the [ViewModelProvider.Factory] that is then used to get a reference to
     * [ViewModel] objects.
     */
    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return NearbyViewModelFactory(provideGithubRepository())
    }

    private fun provideSearchRepository(): SearchRepository {
        return SearchRepository(RetrofitService.create())
    }


    fun provideSearchViewModelFactory(): ViewModelProvider.Factory {
        return SearchViewModelFactory(provideSearchRepository())
    }
}