package com.android.fhrsuk.favourites

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavouritesTable::class], version = 3, exportSchema = false)
abstract class FavouritesDatabase: RoomDatabase() {
    abstract val favouritesDao: FavouritesDao

    companion object{

        @Volatile
        private var INSTANCE: FavouritesDatabase? = null

        fun getInstance(context: Context): FavouritesDatabase{
            synchronized(this){
                var instance = INSTANCE

                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FavouritesDatabase::class.java,
                        "favourite_establishments_table"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}