package com.android.fhrsuk.search

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.android.fhrsuk.models.EstablishmentDetail

class SearchDataSourceFactory(
    private var context: Context,
    private var name: String,
    private var location: String
) : DataSource.Factory<Int, EstablishmentDetail>() {

    private val searchLiveDataSource =
        MutableLiveData<PageKeyedDataSource<Int, EstablishmentDetail>>()

    override fun create(): DataSource<Int, EstablishmentDetail> {

        val dataSource = SearchRepository(
            context,
            name,
            location
        )
        searchLiveDataSource.postValue(dataSource)
        return dataSource
    }

    fun getItemLiveDataSource(): MutableLiveData<PageKeyedDataSource<Int, EstablishmentDetail>> {
        return searchLiveDataSource
    }
}