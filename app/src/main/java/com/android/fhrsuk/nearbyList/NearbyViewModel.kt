package com.android.fhrsuk.nearbyList

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.android.fhrsuk.models.Establishments

class NearbyViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var longitude: String
    private lateinit var latitude: String

    lateinit var itemPagedList: LiveData<PagedList<Establishments>>
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, Establishments>>
    private lateinit var itemDataSourceFactory: NearbyDataSourceFactory

    private val loadingState = NearbyLoadingState.loadingState
    val nearbyLoadingState = MutableLiveData(0)
    lateinit var stateObserver: Observer<Int>

    fun init() {

        itemDataSourceFactory = NearbyDataSourceFactory(
            this.getApplication(),
            longitude,
            latitude
        )

        liveDataSource = itemDataSourceFactory.getItemLiveDataSource()

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(50)
            .build()

        itemPagedList = LivePagedListBuilder(itemDataSourceFactory, config).build()

        //observe loading state or retrofit in singleton
        stateObserver = Observer { currentState ->
            nearbyLoadingState.value = currentState
        }
        loadingState.observeForever(stateObserver)
    }

    fun setLocation(location: Location) {
        this.longitude = location.longitude.toString()
        this.latitude = location.latitude.toString()
    }

    override fun onCleared() {
        loadingState.removeObserver(stateObserver)
        super.onCleared()
    }
}