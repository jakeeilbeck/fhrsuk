package com.android.fhrsuk.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.Injection
import com.android.fhrsuk.R
import com.android.fhrsuk.RecyclerViewAdapter
import com.android.fhrsuk.databinding.FragmentSearchBinding
import com.android.fhrsuk.utils.toVisibility
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var searchViewModel: SearchViewModel

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

        val adapter = RecyclerViewAdapter(requireContext())
        val recyclerView: RecyclerView = binding.searchRecyclerView

        //stops 'blinking' effect when item is clicked
        recyclerView.itemAnimator?.changeDuration = 0

        recyclerView.layoutManager = LinearLayoutManager(context)

        fabUp.hide()

        searchButton.setOnClickListener {

            searchRestaurantName = searchNameText.text.toString().trim()
            searchLocation = searchLocationText.text.toString().trim()

            //Replace empty search term with empty string / wildcard
            if (searchRestaurantName.isEmpty()) searchRestaurantName = ""
            if (searchLocation.isEmpty()) searchLocation = ""

            searchViewModel.setSearchTerms(searchRestaurantName, searchLocation)


            recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
                header = SearchLoadStateAdapter { adapter.retry() },
                footer = SearchLoadStateAdapter { adapter.retry() }
            )

            adapter.addLoadStateListener { loadState ->
                if (loadState.refresh !is LoadState.NotLoading) {
                    searchBinding?.searchRecyclerView?.visibility = View.GONE
                    searchBinding?.progressbarSearch?.visibility =
                        toVisibility(loadState.refresh is LoadState.Loading)
                    searchBinding?.retryButton?.visibility =
                        toVisibility(loadState.refresh is LoadState.Error)
                } else {
                    searchBinding?.searchRecyclerView?.visibility = View.VISIBLE
                    searchBinding?.progressbarSearch?.visibility = View.GONE
                    searchBinding?.retryButton?.visibility = View.GONE
                }
            }

            //Cancel previous job then create a new one
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                searchViewModel.searchRepo().collectLatest {
                    adapter.submitData(it)
                }
            }

            //clear editText focus on search
            searchNameText.clearFocus()
            searchLocationText.clearFocus()

            //hide keyboard
            (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                view.windowToken,
                0
            )
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
        fabUp.setOnClickListener {
            recyclerView.scrollToPosition(0)
        }

    }
}