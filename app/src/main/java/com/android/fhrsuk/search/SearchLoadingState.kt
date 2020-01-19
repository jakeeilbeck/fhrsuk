package com.android.fhrsuk.search

import androidx.lifecycle.MutableLiveData

//singleton used to communicate loading state between repository and ViewModel/Fragment
object SearchLoadingState {
    var loadingState = MutableLiveData<Int>()

    init {
        loadingState.value = 0
    }

    fun setLoadingState(currentState: Int){
        loadingState.postValue(currentState)
    }
}