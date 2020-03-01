package com.android.fhrsuk.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.android.fhrsuk.models.Establishments

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var name: String
    private lateinit var location: String

    lateinit var itemPagedList: LiveData<PagedList<Establishments>>
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, Establishments>>
    private lateinit var itemDataSourceFactory: SearchDataSourceFactory

    private val loadingState = SearchLoadingState.loadingState
    val searchLoadingState = MutableLiveData(0)
    lateinit var stateObserver: Observer<Int>

    fun init() {

        itemDataSourceFactory = SearchDataSourceFactory(
            this.getApplication(),
            name,
            location
        )

        liveDataSource = itemDataSourceFactory.getItemLiveDataSource()

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(50)
            .build()

        itemPagedList = LivePagedListBuilder(itemDataSourceFactory, config).build()

        //observe loading state or retrofit in singleton
        stateObserver = Observer { currentState ->
            searchLoadingState.value = currentState
        }
        loadingState.observeForever(stateObserver)
    }

    fun setSearchTerms(name: String, location: String) {
        this.name = name
        this.location = location
    }

    override fun onCleared() {
        loadingState.removeObserver(stateObserver)
        super.onCleared()
    }
}