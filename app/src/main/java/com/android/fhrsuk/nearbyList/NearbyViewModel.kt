package com.android.fhrsuk.nearbyList

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.android.fhrsuk.models.EstablishmentDetail

class NearbyViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var longitude: String
    private lateinit var latitude: String

    lateinit var itemPagedList: LiveData<PagedList<EstablishmentDetail>>
    lateinit var liveDataSource: LiveData<PageKeyedDataSource<Int, EstablishmentDetail>>
    private lateinit var itemDataSourceFactory: NearbyDataSourceFactory

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
    }

    fun setLocation(location: Location) {
        this.longitude = location.longitude.toString()
        this.latitude = location.latitude.toString()
    }
}