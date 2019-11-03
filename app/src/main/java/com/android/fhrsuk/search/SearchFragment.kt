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
import com.google.android.material.snackbar.Snackbar


class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EstablishmentAdapter

    private lateinit var searchViewModel: SearchViewModel

    private lateinit var searchNameView: EditText
    private lateinit var searchLocationView: EditText
    private lateinit var searchButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var searchRestaurantName: String
    private lateinit var searchLocation: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchNameView = view.findViewById(R.id.edit_text_name)
        searchLocationView = view.findViewById(R.id.edit_text_location)
        searchButton = view.findViewById(R.id.button_search)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressbar_search)
        showProgressBar(false)

        adapter = EstablishmentAdapter(requireContext())
        recyclerView = view.findViewById(R.id.search_recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        searchButton.setOnClickListener {

            showProgressBar(true)
            searchRestaurantName = searchNameView.text.toString().trim()
            searchLocation = searchLocationView.text.toString().trim()

            if (searchRestaurantName.isEmpty() || searchLocation.isEmpty()) {
                Snackbar.make(
                    this.view!!,
                    getString(R.string.search_missing_text),
                    Snackbar.LENGTH_LONG
                ).show()
                showProgressBar(false)
            } else {

                searchViewModel.setSearchTerms(searchRestaurantName, searchLocation)
                searchViewModel.init()

                searchViewModel.itemPagedList.observe(this,
                    Observer<PagedList<EstablishmentDetail>> { items ->
                        items?.let {

                            recyclerView.adapter = adapter
                            adapter.submitList(items)
                            adapter.notifyDataSetChanged()
                            showProgressBar(false)
                        }
                    })
            }
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