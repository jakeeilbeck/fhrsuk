package com.android.fhrsuk.favourites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fhrsuk.favourites.data.FavouritesDao
import com.android.fhrsuk.favourites.data.FavouritesTable
import kotlinx.coroutines.launch

class FavouritesViewModel(private val favouritesDatabase: FavouritesDao) : ViewModel() {

    fun addRemoveFromFavourites(favourite: FavouritesTable?){
        viewModelScope.launch {
            val newFavourite = FavouritesTable(
                favourite?.fHRSID,
                favourite?.businessName,
                favourite?.businessType,
                favourite?.addressLine1,
                favourite?.addressLine2,
                favourite?.postCode,
                favourite?.ratingValue,
                favourite?.ratingDate,
                favourite?.hygiene,
                favourite?.structural,
                favourite?.confidenceInManagement,
                System.currentTimeMillis())
            addRemove(newFavourite)
        }
    }

    private suspend fun addRemove(favourite: FavouritesTable){
        if(favouritesDatabase.checkExists(favourite.fHRSID) == 0){
            favouritesDatabase.insert(favourite)
        }else{
            favouritesDatabase.delete(favourite)
        }
    }

    fun getAllFavourites(): LiveData<List<FavouritesTable>> {
        return favouritesDatabase.getAll()
    }
}