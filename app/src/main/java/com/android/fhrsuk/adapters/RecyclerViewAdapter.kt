package com.android.fhrsuk.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.databinding.ListItemBinding
import com.android.fhrsuk.models.Establishments

class RecyclerViewAdapter(private var context: Context, val favouritesClick: (Establishments?) -> Unit) :
    PagingDataAdapter<Establishments, RecyclerViewAdapter.ViewHolder>(diffCallback) {

    private val adapterUtils = RecyclerAdapterUtils(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val listItemBinding = ListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(listItemBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val establishmentDetail: Establishments? = getItem(position)
        viewHolder.nameTextView.text = establishmentDetail!!.businessName
        viewHolder.ratingTextView.text = adapterUtils.getRating(establishmentDetail.ratingValue)
        viewHolder.inspectionDateTextView.text = adapterUtils.getDate(establishmentDetail.ratingValue, establishmentDetail.ratingDate)
        viewHolder.address1TextView.text = establishmentDetail.addressLine1
        viewHolder.address2TextView.text = establishmentDetail.addressLine2
        viewHolder.postcodeTextView.text = establishmentDetail.postCode
        viewHolder.businessTypeTextView.text = establishmentDetail.businessType
        viewHolder.scoreBreakdownHygiene.text = adapterUtils.getBreakdownHygiene(establishmentDetail.scores.hygiene)
        viewHolder.scoreBreakdownStructural.text = adapterUtils.getBreakdownStructural(establishmentDetail.scores.structural)
        viewHolder.scoreBreakdownManagement.text = adapterUtils.getBreakdownManagement(establishmentDetail.scores.confidenceInManagement)

        //Set rating background square colour based on rating value
        val ratingBg = viewHolder.ratingTextView.background as GradientDrawable
        ratingBg.setColor(adapterUtils.getRatingBgColour(establishmentDetail.ratingValue))

        viewHolder.cardView.setOnClickListener {
            //Current expanded state
            val expanded: Boolean = establishmentDetail.isExpanded
            //Update the expanded state
            establishmentDetail.isExpanded = !expanded

            notifyItemChanged(position)
        }

        //Item expanded state
        val expanded: Boolean = establishmentDetail.isExpanded
        //Set the visibility based on state and animate indicator arrow icon
        if (expanded) {
            viewHolder.expandableAdditionalInfo.visibility = View.VISIBLE
            viewHolder.indicatorIcon.animate().rotation(180F).start()
        } else {
            viewHolder.indicatorIcon.animate().rotation(0F).start()
            viewHolder.expandableAdditionalInfo.visibility = View.GONE
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Establishments>() {

            override fun areItemsTheSame(
                oldItem: Establishments,
                newItem: Establishments
            ): Boolean = oldItem.fHRSID == newItem.fHRSID

            override fun areContentsTheSame(
                oldItem: Establishments,
                newItem: Establishments
            ): Boolean = oldItem == newItem

        }
    }

    inner class ViewHolder(listItemBinding: ListItemBinding) :
        RecyclerView.ViewHolder(listItemBinding.root) {
        val nameTextView: TextView = listItemBinding.name
        val ratingTextView: TextView = listItemBinding.rating
        val address1TextView: TextView = listItemBinding.address1
        val address2TextView: TextView = listItemBinding.address2
        val postcodeTextView: TextView = listItemBinding.postcode
        val inspectionDateTextView: TextView = listItemBinding.inspectionDate
        val expandableAdditionalInfo: View = listItemBinding.expandableAdditionalInfo
        val cardView: CardView = listItemBinding.cardView
        val businessTypeTextView: TextView = listItemBinding.businessType
        val scoreBreakdownHygiene: TextView = listItemBinding.scoreBreakdownHygiene
        val scoreBreakdownStructural: TextView = listItemBinding.scoreBreakdownStructural
        val scoreBreakdownManagement: TextView = listItemBinding.scoreBreakdownManagement
        val indicatorIcon: ImageView = listItemBinding.expandCollapseIndicator
        init {
            listItemBinding.favButton.setOnClickListener {
                favouritesClick.invoke(getItem(layoutPosition))
            }
        }
    }
}