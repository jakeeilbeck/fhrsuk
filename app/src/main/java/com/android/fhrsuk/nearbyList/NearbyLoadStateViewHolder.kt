package com.android.fhrsuk.nearbyList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.R
import com.android.fhrsuk.databinding.LoadStateFooterBinding
import com.android.fhrsuk.utils.toVisibility

class NearbyLoadStateViewHolder (
    private val binding: LoadStateFooterBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.visibility =
            toVisibility(loadState is LoadState.Loading)
        binding.retryButton.visibility =
            toVisibility(loadState !is LoadState.Loading)
        binding.errorMsg.visibility =
            toVisibility(loadState !is LoadState.Loading)
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): NearbyLoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.load_state_footer, parent, false)
            val binding = LoadStateFooterBinding.bind(view)
            return NearbyLoadStateViewHolder(binding, retry)
        }
    }
}
