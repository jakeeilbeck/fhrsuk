package com.android.fhrsuk.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.Injection
import com.android.fhrsuk.R
import com.android.fhrsuk.adapters.RecyclerViewAdapter
import com.android.fhrsuk.databinding.FragmentSearchBinding
import com.android.fhrsuk.favourites.data.FavouritesDatabase
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.search.loadingState.SearchLoadStateAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter
//    private var searchBinding: FragmentSearchBinding? = null
    private var searchJob: Job? = null

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val favouritesDataSource = FavouritesDatabase.getInstance(application).favouritesDao
        val viewModelFactory = Injection.provideSearchViewModelFactory(favouritesDataSource)

        searchViewModel = ViewModelProvider(this, viewModelFactory)
            .get(SearchViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val binding = FragmentSearchBinding.bind(view)
//        searchBinding = binding

        val searchNameText = binding.editTextName
        val searchLocationText = binding.editTextLocation
        val searchButton = binding.buttonSearch
        val fabUp = binding.fabUp

        var searchRestaurantName: String
        var searchLocation: String

        adapter = RecyclerViewAdapter(requireContext()) { establishment: Establishments? ->
            favouritesOnClick(establishment)
        }

        //fabUp will only be visible after the user has started scrolling
        fabUp.hide()

        if (searchViewModel.getCurrentSearchResult() != null){
            getEstablishments(false)
        }

        searchButton.setOnClickListener {

            searchRestaurantName = searchNameText.text.toString().trim()
            searchLocation = searchLocationText.text.toString().trim()

            if (searchRestaurantName.isEmpty() && searchLocation.isEmpty()){
                //Hide keyboard first to make Toast more visible (if open)
                hideKeyboard(view)
                Toast.makeText(context, "Please enter at least 1 search term", Toast.LENGTH_SHORT).show()
            }else{
                //Replace empty search term with empty string / wildcard
                if (searchRestaurantName.isEmpty()) searchRestaurantName = ""
                if (searchLocation.isEmpty()) searchLocation = ""

                searchViewModel.setSearchTerms(searchRestaurantName, searchLocation)

                //Cancels previous job then create a new one to get data
                getEstablishments(true)

                //clear editText focus on search
                searchNameText.clearFocus()
                searchLocationText.clearFocus()

                hideKeyboard(view)
            }
        }

        recyclerView = binding.searchRecyclerView

        //stops 'blinking' effect when item is clicked
        recyclerView.itemAnimator?.changeDuration = 0

        //show/hide the fab button after scrolled passed ~1 page of results
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

        //scroll to top of list on click
        fabUp.setOnClickListener { lifecycleScope.launch { recyclerView.scrollToPosition(0) } }

        binding.retryButton.setOnClickListener { adapter.retry() }
    }

    private fun favouritesOnClick(establishment: Establishments?){
        Log.i("NearbyFragment", "favButton?.setOnClickListener")
        searchViewModel.addRemoveFromFavourites(establishment)
    }

    private fun hideKeyboard(view: View){
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view.windowToken,
            0
        )
    }

    private fun getEstablishments(getNewData: Boolean){
        if (getNewData){
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                searchViewModel.searchEstablishments().collectLatest {
                    adapter.submitData(it)
                }
            }
        }else{
            lifecycleScope.launch {
                searchViewModel.getCurrentSearchResult()?.collectLatest {
                    adapter.submitData(it)
                }
            }
        }
        initAdapter()
    }

    private fun initAdapter() {
        //Display progressBar or retry button for loading of data or failure of loading
        binding.searchRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = SearchLoadStateAdapter { adapter.retry() },
            footer = SearchLoadStateAdapter { adapter.retry() }
        )

        //show / hide the header or footer views based on loading state
        adapter.addLoadStateListener { loadState ->

            binding.searchRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
            // Show progress bar during initial load or refresh.
            binding.progressbarSearch.isVisible = loadState.source.refresh is LoadState.Loading
            // Show the retry button if initial load or refresh fails.
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error
            // Show message if no results
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount <1){
                recyclerView.isVisible = false
                binding.noResultsText.isVisible = true
            }else{
                binding.noResultsText.isVisible = false
            }

        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}