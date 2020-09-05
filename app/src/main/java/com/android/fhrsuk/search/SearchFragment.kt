package com.android.fhrsuk.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
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
import com.android.fhrsuk.search.loadingState.SearchLoadStateAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter
    private var searchBinding: FragmentSearchBinding? = null
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchViewModel = ViewModelProvider(this, Injection.provideSearchViewModelFactory())
            .get(SearchViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSearchBinding.bind(view)
        searchBinding = binding

        val searchNameText: EditText = binding.editTextName
        val searchLocationText: EditText = binding.editTextLocation
        val searchButton: Button = binding.buttonSearch
        val fabUp: FloatingActionButton = binding.fabUp

        var searchRestaurantName: String
        var searchLocation: String

        adapter = RecyclerViewAdapter(requireContext())
        recyclerView = binding.searchRecyclerView

        initAdapter()

        //stops 'blinking' effect when item is clicked
        recyclerView.itemAnimator?.changeDuration = 0

        //fabUp will only be visible after the user has started scrolling
        fabUp.hide()

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
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    searchViewModel.searchEstablishments().collectLatest {
                        adapter.submitData(it)
                    }
                }

                //clear editText focus on search
                searchNameText.clearFocus()
                searchLocationText.clearFocus()

                hideKeyboard(view)
            }
        }

        //https://git.io/JUsKp
        //Scroll to top of list on refresh
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect{binding.searchRecyclerView.scrollToPosition(0)}
        }

        searchBinding?.retryButton?.setOnClickListener { adapter.retry() }

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
    }

    private fun hideKeyboard(view: View){
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view.windowToken,
            0
        )
    }

    private fun initAdapter() {
        //Display progressBar or retry button for loading of data or failure of loading
        recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = SearchLoadStateAdapter { adapter.retry() },
            footer = SearchLoadStateAdapter { adapter.retry() }
        )

        //show / hide the header or footer views based on loading state
        adapter.addLoadStateListener { loadState ->

            searchBinding?.searchRecyclerView?.isVisible = loadState.source.refresh is LoadState.NotLoading
            // Show progress bar during initial load or refresh.
            searchBinding?.progressbarSearch?.isVisible = loadState.source.refresh is LoadState.Loading
            // Show the retry button if initial load or refresh fails.
            searchBinding?.retryButton?.isVisible = loadState.source.refresh is LoadState.Error
            // Show message if no results
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount <1){
                recyclerView.isVisible = false
                no_results_text.isVisible = true
            }else{
                no_results_text.isVisible = false
            }

        }
    }

    override fun onDestroyView() {
        searchBinding = null
        super.onDestroyView()
    }
}