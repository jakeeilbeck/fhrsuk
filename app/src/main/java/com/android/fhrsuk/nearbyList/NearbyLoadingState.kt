package com.android.fhrsuk.nearbyList

import androidx.lifecycle.MutableLiveData

//singleton used to communicate loading state between repository and ViewModel/Fragment
object NearbyLoadingState {
    var loadingState = MutableLiveData<Int>()

    init {
        loadingState.value = 0
    }

    fun setLoadingState(currentState: Int){
        loadingState.postValue(currentState)
    }
}