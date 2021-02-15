package com.android.fhrsuk.favourites

import androidx.room.*

@Entity(tableName = "favourite_establishments_table")
data class FavouritesTable(

    @PrimaryKey
    var fHRSID: Int?,

    @ColumnInfo(name = "business_name")
    var businessName: String?,

    @ColumnInfo(name = "business_type")
    var businessType: String?,

    @ColumnInfo(name = "address_1")
    var addressLine1: String?,

    @ColumnInfo(name = "address_2")
    var addressLine2: String?,

    @ColumnInfo(name = "address_postcode")
    var postCode: String?,

    @ColumnInfo(name = "rating_value")
    var ratingValue: String?,

    @ColumnInfo(name = "rating_date")
    var ratingDate: String?,

    @ColumnInfo(name = "hygiene")
    val hygiene: Int?,

    @ColumnInfo(name = "structural")
    val structural: Int?,

    @ColumnInfo(name = "confidence_in_management")
    val confidenceInManagement: Int?,

    @ColumnInfo(name = "time_added")
    val timeAdded: Long,

    @ColumnInfo(name = "item_expanded")
    var itemExpanded: Boolean = false
)