package com.android.fhrsuk.search


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.android.fhrsuk.models.EstablishmentDetail

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var name: String
    private lateinit var location: String

    lateinit var itemPagedList: LiveData<PagedList<EstablishmentDetail>>
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, EstablishmentDetail>>

    private lateinit var itemDataSourceFactory: SearchDataSourceFactory

    fun init() {
        itemDataSourceFactory = SearchDataSourceFactory(
            this.getApplication(),
            name,
            location
        )
        this.liveDataSource = itemDataSourceFactory.getItemLiveDataSource()

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(50)
            .build()

        this.itemPagedList = LivePagedListBuilder(itemDataSourceFactory, config).build()
    }

    fun setSearchTerms(name: String, location: String) {
        this.name = name
        this.location = location
    }

}