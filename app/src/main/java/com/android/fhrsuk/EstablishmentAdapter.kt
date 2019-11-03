package com.android.fhrsuk

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.models.EstablishmentDetail

class EstablishmentAdapter(
    private var context: Context
) : PagedListAdapter<EstablishmentDetail, EstablishmentAdapter.ViewHolder>(
    diffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val establishmentDetail: EstablishmentDetail? = getItem(position)
        viewHolder.nameTextView.text = establishmentDetail!!.businessName
        viewHolder.address1TextView.text = establishmentDetail.addressLine1
        viewHolder.address2TextView.text = establishmentDetail.addressLine2
        viewHolder.postcodeTextView.text = establishmentDetail.postCode

        when {
            establishmentDetail.ratingValue == "Exempt" -> {
                viewHolder.ratingTextView.text = "N/A"
                viewHolder.inspectionDateTextView.text = context.getString(R.string.rating_exempt)
            }
            establishmentDetail.ratingValue == "AwaitingPublication" ||
                    establishmentDetail.ratingValue == "Awaiting Publication" -> {
                viewHolder.ratingTextView.text = "N/A"
                viewHolder.inspectionDateTextView.text =
                    context.getString(R.string.rating_awaiting_publication)
            }
            establishmentDetail.ratingValue == "AwaitingInspection" ||
                    establishmentDetail.ratingValue == "Awaiting Inspection" -> {
                viewHolder.ratingTextView.text = "N/A"
                viewHolder.inspectionDateTextView.text =
                    context.getString(R.string.rating_awaiting_inspection)
            }
            establishmentDetail.ratingValue == "Pass and Eat Safe" -> {
                viewHolder.ratingTextView.text = context.getString(R.string.rating_pass)
                viewHolder.inspectionDateTextView.text = establishmentDetail.ratingDate
            }
            establishmentDetail.ratingValue == "Improvement Required" -> {
                viewHolder.ratingTextView.text = "3"
                viewHolder.inspectionDateTextView.text = establishmentDetail.ratingDate
            }
            else -> {
                viewHolder.ratingTextView.text = establishmentDetail.ratingValue
                viewHolder.inspectionDateTextView.text = establishmentDetail.ratingDate
            }
        }

        val ratingBg = viewHolder.ratingTextView.background as GradientDrawable
        val ratingColour = getRatingColour(establishmentDetail.ratingValue)
        ratingBg.setColor(ratingColour)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<EstablishmentDetail>() {

            override fun areItemsTheSame(
                oldItem: EstablishmentDetail,
                newItem: EstablishmentDetail
            ):
                    Boolean = oldItem.fHRSID == newItem.fHRSID

            override fun areContentsTheSame(
                oldItem: EstablishmentDetail,
                newItem: EstablishmentDetail
            ):
                    Boolean = oldItem == newItem
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameTextView: TextView = itemView.findViewById(R.id.name)
        var ratingTextView: TextView = itemView.findViewById(R.id.rating)
        var address1TextView: TextView = itemView.findViewById(R.id.address1)
        var address2TextView: TextView = itemView.findViewById(R.id.address2)
        var postcodeTextView: TextView = itemView.findViewById(R.id.postcode)
        var inspectionDateTextView: TextView = itemView.findViewById(R.id.inspection_date)
    }


    private fun getRatingColour(rating: String): Int {
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
}