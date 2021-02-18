package com.android.fhrsuk.adapters

import android.content.Context
import androidx.core.content.ContextCompat
import com.android.fhrsuk.R
import java.time.format.DateTimeFormatter.ofPattern

class RecyclerAdapterUtils(var context: Context) {

    fun getBreakdownHygiene(rating: Int?): String {
        return when (rating) {
            0 -> {
                context.getString(R.string.score_breakdown_very_good)
            }
            5 -> {
                context.getString(R.string.score_breakdown_good)
            }
            10 -> {
                context.getString(R.string.score_breakdown_generally_satisfactory)
            }
            15 -> {
                context.getString(R.string.score_breakdown_improvement_necessary)
            }
            20 -> {
                context.getString(R.string.score_breakdown_major_improvement_necessary)
            }
            25 -> {
                context.getString(R.string.score_breakdown_urgent_improvement_necessary)
            }
            else -> "N/A"
        }
    }

    fun getBreakdownStructural(rating: Int?): String {
        return when (rating) {
            0 -> {
                context.getString(R.string.score_breakdown_very_good)
            }
            5 -> {
                context.getString(R.string.score_breakdown_good)
            }
            10 -> {
                context.getString(R.string.score_breakdown_generally_satisfactory)
            }
            15 -> {
                context.getString(R.string.score_breakdown_improvement_necessary)
            }
            20 -> {
                context.getString(R.string.score_breakdown_major_improvement_necessary)
            }
            25 -> {
                context.getString(R.string.score_breakdown_urgent_improvement_necessary)
            }
            else -> context.getString(R.string.not_available)
        }
    }

    fun getBreakdownManagement(rating: Int?): String {
        return when (rating) {
            0 -> {
                context.getString(R.string.score_breakdown_very_good)
            }
            5 -> {
                context.getString(R.string.score_breakdown_good)
            }
            10 -> {
                context.getString(R.string.score_breakdown_generally_satisfactory)
            }
            20 -> {
                context.getString(R.string.score_breakdown_major_improvement_necessary)
            }
            30 -> {
                context.getString(R.string.score_breakdown_urgent_improvement_necessary)
            }
            else -> context.getString(R.string.not_available)
        }
    }

    //display N/A when rating isn't available
    fun getRating(rating: String): String {
        return when (rating) {
            "Exempt", "AwaitingPublication", "Awaiting Publication", "AwaitingInspection", "Awaiting Inspection" -> {
                context.getString(R.string.not_available)
            }
            "Pass and Eat Safe" -> {
                context.getString(R.string.rating_pass)
            }
            "Improvement Required" -> {
                "3"
            }
            else -> {
                rating
            }
        }
    }

    //If rating isn't available show reason why in place of the date
    fun getDate(rating: String, ratingDate: String): String {

        val currentFormat = ofPattern("yyyy-MM-dd'T'HH:m:ss")
        val date = currentFormat.parse(ratingDate)
        val ratingDateFormatted = ofPattern("dd MMM yyyy").format(date)

        return when (rating) {
            "Exempt" -> context.getString(R.string.rating_exempt)
            "AwaitingPublication", "Awaiting Publication" -> context.getString(R.string.rating_awaiting_publication)
            "AwaitingInspection", "Awaiting Inspection" -> context.getString(R.string.rating_awaiting_inspection)
            "Pass and Eat Safe" -> ratingDateFormatted
            "Improvement Required" -> ratingDateFormatted
            else -> ratingDateFormatted
        }
    }

    //set rating background colour based on rating value
    fun getRatingBgColour(rating: String): Int {
        val ratingColourResourceId: Int = when (rating) {
            "0" -> R.color.rating0
            "1" -> R.color.rating1
            "2" -> R.color.rating2
            "3", "Improvement Required" -> R.color.rating3
            "4" -> R.color.rating4
            "5", "Pass", "Pass and Eat Safe" -> R.color.rating5
            "Exempt", "AwaitingPublication", "AwaitingInspection" -> R.color.ratingNa
            else -> R.color.ratingNa
        }
        return ContextCompat.getColor(context, ratingColourResourceId)
    }

    fun setFavouriteColour(isFavourite: Boolean): Int{
        return if (isFavourite){
            ContextCompat.getColor(context, R.color.favouriteIcon)
        }else{
            ContextCompat.getColor(context, R.color.colorPrimary)
        }
    }
}