package com.android.fhrsuk.nearbyList.loadingState

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class NearbyLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<NearbyLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: NearbyLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NearbyLoadStateViewHolder {
        return NearbyLoadStateViewHolder.create(
            parent,
            retry
        )
    }
}