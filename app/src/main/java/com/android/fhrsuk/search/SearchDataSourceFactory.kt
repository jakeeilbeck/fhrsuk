package com.android.fhrsuk.search

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.android.fhrsuk.models.Establishments

class SearchDataSourceFactory(
    private var context: Context,
    private var name: String,
    private var location: String
) : DataSource.Factory<Int, Establishments>() {

    private val searchLiveDataSource =
        MutableLiveData<PageKeyedDataSource<Int, Establishments>>()

    override fun create(): DataSource<Int, Establishments> {

        val dataSource = SearchRepository(
            context,
            name,
            location
        )
        searchLiveDataSource.postValue(dataSource)
        return dataSource
    }

    fun getItemLiveDataSource(): MutableLiveData<PageKeyedDataSource<Int, Establishments>> {
        return searchLiveDataSource
    }
}