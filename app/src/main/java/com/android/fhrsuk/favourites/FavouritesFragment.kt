package com.android.fhrsuk.favourites

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.Injection
import com.android.fhrsuk.R
import com.android.fhrsuk.databinding.FragmentFavouritesBinding
import kotlinx.coroutines.launch

class FavouritesFragment: Fragment(R.layout.fragment_favourites) {

    private lateinit var favouritesViewModel: FavouritesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavouritesAdapter
    private var favouriteBinding: FragmentFavouritesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val favouritesDataSource = FavouritesDatabase.getInstance(application).favouritesDao
        val viewModelFactory = Injection.provideFavouritesViewModelFactory(favouritesDataSource)

        favouritesViewModel = ViewModelProvider(this, viewModelFactory)
            .get(FavouritesViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFavouritesBinding.bind(view)
        favouriteBinding = binding
        recyclerView = binding.favouritesRecyclerView
        val progressBar = binding.progressbarFavourites
        val fabUp = binding.fabUp

        progressBar.isVisible = true

        //stops 'blinking' effect when item is clicked
        recyclerView.itemAnimator?.changeDuration = 0

        // Show/hide the fabUp button after scrolled passed ~1 page of results
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.computeVerticalScrollOffset() > 1500) {
                    fabUp.show()
                } else {
                    fabUp.hide()
                }
            }
        })

        fabUp.setOnClickListener { scrollToTop() }

        adapter = FavouritesAdapter(requireContext()) { establishment: FavouritesTable? ->
            favouritesOnClick(establishment)
        }

        lifecycleScope.launch {
            favouritesViewModel.getAllFavourites().observe(viewLifecycleOwner, {
                recyclerView.adapter = adapter
                adapter.submitList(it)
                progressBar.isVisible = false
            })
        }
    }

    private fun favouritesOnClick(establishment: FavouritesTable?){
        Log.i("NearbyFragment", "favButton?.setOnClickListener")
        favouritesViewModel.addRemoveFromFavourites(establishment)
    }

    fun scrollToTop(){
        favouriteBinding?.favouritesRecyclerView?.scrollToPosition(0)
    }
}