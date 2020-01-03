package com.android.fhrsuk

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        viewHolder.additionalBusinessTypeTextView.text = establishmentDetail.businessType
        viewHolder.additionalLocalAuthorityName.text = establishmentDetail.localAuthorityName
        viewHolder.additionalLocalAuthorityWebsite.text = establishmentDetail.localAuthorityWebSite
        viewHolder.additionalLocalAuthorityEmaile.text =
            establishmentDetail.localAuthorityEmailAddress

        //display N/A when rating isn't available, and show the reason why where the date would otherwise be
        when (establishmentDetail.ratingValue) {
            "Exempt" -> {
                viewHolder.ratingTextView.text = "N/A"
                viewHolder.inspectionDateTextView.text = context.getString(R.string.rating_exempt)
            }
            "AwaitingPublication", "Awaiting Publication" -> {
                viewHolder.ratingTextView.text = "N/A"
                viewHolder.inspectionDateTextView.text =
                    context.getString(R.string.rating_awaiting_publication)
            }
            "AwaitingInspection", "Awaiting Inspection" -> {
                viewHolder.ratingTextView.text = "N/A"
                viewHolder.inspectionDateTextView.text =
                    context.getString(R.string.rating_awaiting_inspection)
            }
            "Pass and Eat Safe" -> {
                viewHolder.ratingTextView.text = context.getString(R.string.rating_pass)
                viewHolder.inspectionDateTextView.text = establishmentDetail.ratingDate
            }
            "Improvement Required" -> {
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


        viewHolder.cardView.setOnClickListener {
            //Current expanded state
            val expanded: Boolean = establishmentDetail.isExpanded
            //Update the expanded state
            establishmentDetail.isExpanded = !expanded

            notifyItemChanged(position)
        }

        //Item expanded state
        val expanded: Boolean = establishmentDetail.isExpanded
        // Set the visibility based on state
        viewHolder.expandableAdditionalInfo.visibility = (if (expanded) View.VISIBLE else View.GONE)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<EstablishmentDetail>() {

            override fun areItemsTheSame(
                oldItem: EstablishmentDetail,
                newItem: EstablishmentDetail
            ): Boolean = oldItem.fHRSID == newItem.fHRSID

            override fun areContentsTheSame(
                oldItem: EstablishmentDetail,
                newItem: EstablishmentDetail
            ): Boolean = oldItem == newItem
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val ratingTextView: TextView = itemView.findViewById(R.id.rating)
        val address1TextView: TextView = itemView.findViewById(R.id.address1)
        val address2TextView: TextView = itemView.findViewById(R.id.address2)
        val postcodeTextView: TextView = itemView.findViewById(R.id.postcode)
        val inspectionDateTextView: TextView = itemView.findViewById(R.id.inspection_date)
        val expandableAdditionalInfo: View = itemView.findViewById(R.id.expandable_additional_info)
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val additionalBusinessTypeTextView: TextView =
            itemView.findViewById(R.id.additional_business_type)
        val additionalLocalAuthorityName: TextView = itemView.findViewById(R.id.local_authority)
        val additionalLocalAuthorityWebsite: TextView =
            itemView.findViewById(R.id.local_authority_website)
        val additionalLocalAuthorityEmaile: TextView =
            itemView.findViewById(R.id.local_authority_email)
    }

    //set rating background colour based on rating value
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