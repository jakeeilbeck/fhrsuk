package com.android.fhrsuk.nearbyList

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.android.fhrsuk.models.EstablishmentDetail

//DataSourceFactory for paging implementation
class NearbyDataSourceFactory(
    private var context: Context,
    private var longitude: String,
    private var latitude: String
) : DataSource.Factory<Int, EstablishmentDetail>() {

    private val listLiveDataSource =
        MutableLiveData<PageKeyedDataSource<Int, EstablishmentDetail>>()

    override fun create(): DataSource<Int, EstablishmentDetail> {

        val dataSource = NearbyRepository(
            context,
            longitude,
            latitude
        )
        listLiveDataSource.postValue(dataSource)
        return dataSource
    }

    fun getItemLiveDataSource(): MutableLiveData<PageKeyedDataSource<Int, EstablishmentDetail>> {
        return listLiveDataSource
    }
}