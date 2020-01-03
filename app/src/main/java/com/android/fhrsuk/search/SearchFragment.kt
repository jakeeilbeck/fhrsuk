package com.android.fhrsuk.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.fhrsuk.EstablishmentAdapter
import com.android.fhrsuk.R
import com.android.fhrsuk.models.EstablishmentDetail
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchNameText: EditText = view.findViewById(R.id.edit_text_name)
        val searchLocationText: EditText = view.findViewById(R.id.edit_text_location)
        val searchButton: Button = view.findViewById(R.id.button_search)

        val fabUp: FloatingActionButton = view.findViewById(R.id.fab_up)

        var searchRestaurantName: String
        var searchLocation: String

        val adapter = EstablishmentAdapter(requireContext())
        val recyclerView: RecyclerView = view.findViewById(R.id.search_recyclerView)

        progressBar = view.findViewById(R.id.progressbar_search)
        //showProgressBar(false)

        //stops 'blinking' effect when item is clicked
        recyclerView.itemAnimator?.changeDuration = 0

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        fabUp.hide()

        searchButton.setOnClickListener {

            showProgressBar(true)
            recyclerView.visibility = View.INVISIBLE

            searchRestaurantName = searchNameText.text.toString().trim()
            searchLocation = searchLocationText.text.toString().trim()

            //Replace empty search term with wildcard
            if (searchRestaurantName.isEmpty()) searchRestaurantName = "^"
            if (searchLocation.isEmpty()) searchLocation = "^"

            searchViewModel.setSearchTerms(searchRestaurantName, searchLocation)
            searchViewModel.init()

            //Refreshes views with new data
            searchViewModel.itemPagedList.observe(this,
                Observer<PagedList<EstablishmentDetail>> { items ->
                    items?.let {

                        recyclerView.adapter = adapter
                        adapter.submitList(items)
                        adapter.notifyDataSetChanged()

                        showProgressBar(false)
                        recyclerView.visibility = View.VISIBLE

                    }
                }
            )

            //clear editText focus on search
            searchNameText.clearFocus()
            searchLocationText.clearFocus()
        }

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

    private fun showProgressBar(setVisible: Boolean) {
        if (setVisible) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }
}