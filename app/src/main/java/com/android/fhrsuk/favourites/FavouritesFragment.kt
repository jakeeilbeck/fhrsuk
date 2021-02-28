package com.android.fhrsuk.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.Injection
import com.android.fhrsuk.R
import com.android.fhrsuk.adapters.FavouritesAdapter
import com.android.fhrsuk.databinding.FragmentFavouritesBinding
import com.android.fhrsuk.favourites.data.FavouritesDatabase
import com.android.fhrsuk.favourites.data.FavouritesTable
import kotlinx.coroutines.launch

class FavouritesFragment: Fragment(R.layout.fragment_favourites) {

    private lateinit var favouritesViewModel: FavouritesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavouritesAdapter

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val favouritesDataSource = FavouritesDatabase.getInstance(application).favouritesDao
        val viewModelFactory = Injection.provideFavouritesViewModelFactory(favouritesDataSource)

        favouritesViewModel = ViewModelProvider(this, viewModelFactory)
            .get(FavouritesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            Toast.makeText(requireContext(), getString(R.string.removed_from_favourites),Toast.LENGTH_SHORT).show()
        }

        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        lifecycleScope.launch {
            favouritesViewModel.getAllFavourites().observe(viewLifecycleOwner, {
                adapter.submitList(it)
            })
        }
        progressBar.isVisible = false
        recyclerView.adapter = adapter
    }

    private fun favouritesOnClick(establishment: FavouritesTable?){
        favouritesViewModel.addRemoveFromFavourites(establishment)
    }

    private fun scrollToTop(){
        binding.favouritesRecyclerView.scrollToPosition(0)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}