package com.android.fhrsuk.favourites.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavouritesDao {
    @Insert
    suspend fun insert(establishment: FavouritesTable)

    @Update
    suspend fun update(establishment: FavouritesTable)

    @Delete
    suspend fun delete(establishment: FavouritesTable)

    @Query("SELECT count(fHRSID) FROM favourite_establishments_table WHERE fHRSID = :establishmentID")
    suspend fun checkExists(establishmentID: Int?): Int

    @Query("SELECT * FROM favourite_establishments_table ORDER BY time_added ASC")
    fun getAll(): LiveData<List<FavouritesTable>>

}