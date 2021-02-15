package com.android.fhrsuk.nearbyList

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.fhrsuk.BuildConfig
import com.android.fhrsuk.Injection
import com.android.fhrsuk.R
import com.android.fhrsuk.adapters.RecyclerViewAdapter
import com.android.fhrsuk.databinding.FragmentNearbyListBinding
import com.android.fhrsuk.favourites.FavouritesDatabase
import com.android.fhrsuk.models.Establishments
import com.android.fhrsuk.nearbyList.loadingState.NearbyLoadStateAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private const val PERMISSIONS_REQUEST_CODE = 11
private const val TAG = "NearbyFragment"

//Interface to communicate Fragment visibility to Activity. Used to tell Activity to hide it's
//ProgressBar once fragment loads
interface FragmentVisibleListener {
    fun onFragmentVisible()
}

class NearbyFragment : Fragment(R.layout.fragment_nearby_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var nearbyViewModel: NearbyViewModel
    private lateinit var locationServices: LocationServices
    private var searchJob: Job? = null
    private var nearbyBinding: FragmentNearbyListBinding? = null

    private lateinit var location: Location

    var fragmentVisibleListener: FragmentVisibleListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val favouritesDataSource = FavouritesDatabase.getInstance(application).favouritesDao
        val viewModelFactory = Injection.provideNearbyViewModelFactory(favouritesDataSource)

        nearbyViewModel = ViewModelProvider(this, viewModelFactory)
            .get(NearbyViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNearbyListBinding.bind(view)
        nearbyBinding = binding

        swipeRefresh = binding.swipeRefresh
        val fabUp: FloatingActionButton = binding.fabUp
        val fabFilter: FloatingActionButton = binding.fabFilter
        val filterClear = nearbyBinding?.filterClear
        val filter0 = nearbyBinding?.filterRating0
        val filter1 = nearbyBinding?.filterRating1
        val filter2 = nearbyBinding?.filterRating2
        val filter3 = nearbyBinding?.filterRating3
        val filter4 = nearbyBinding?.filterRating4
        val filter5 = nearbyBinding?.filterRating5

        adapter = RecyclerViewAdapter(requireContext()) { establishment: Establishments? ->
            favouritesOnClick(establishment)
        }

        //fabUp will only be visible after the user has started scrolling
        fabUp.hide()

        //Observer for location updates
        val locationObserver = Observer<Location> { newLocation ->
            location = newLocation

            //Only the initial request of the session should be triggered by a location update
            //Subsequent updates triggered by swipeRefresh
            if (nearbyViewModel.getIsFirstSearch()) {

                nearbyViewModel.setLocation(newLocation)
                getEstablishments(true)

                nearbyViewModel.setIsFirstSearchFalse()
            }
        }

        //Get the existing list of establishments after configuration change
        if (!nearbyViewModel.getIsFirstSearch()) {
            getEstablishments(false)
        }

        //Initialise location updates and observe latest results
        locationServices = LocationServices(this.requireContext())
        locationServices.location.observe(this.viewLifecycleOwner, locationObserver)

        recyclerView = binding.listRecyclerView

        //stops 'blinking' effect when an item is clicked
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

        //Display filter options if they were previously expanded, and don't update the visibility status
        showHideFilters(false)

        fabUp.setOnClickListener {
            scrollToTop()
        }

        //Show/Hide filter options
        fabFilter.setOnClickListener {
            //Show/hide filter options and update the filter status
            showHideFilters(true)
        }

        //Click listeners for each filter option
        filterClear?.setOnClickListener {
            filterList("clear")
        }

        filter0?.setOnClickListener {
            filterList("0")
        }

        filter1?.setOnClickListener {
            filterList("1")
        }

        filter2?.setOnClickListener {
            filterList("2")
        }

        filter3?.setOnClickListener {
            filterList("3")
        }

        filter4?.setOnClickListener {
            filterList("4")
        }

        filter5?.setOnClickListener {
            filterList("5")
        }

        swipeRefresh.setOnRefreshListener {
            //Update ViewModel with latest location then search
            if (this::location.isInitialized) {
                nearbyViewModel.setLocation(location)
                getEstablishments(true)
            }
            scrollToTop()
        }

        nearbyBinding?.retryButton?.setOnClickListener { adapter.retry() }
    }

    private fun favouritesOnClick(establishment: Establishments?){
        Log.i("NearbyFragment", "favButton?.setOnClickListener")
        nearbyViewModel.addRemoveFromFavourites(establishment)
    }

    //Show/Hide filter options, apply fade animation, and update the filter status when parameter is true.
    private fun showHideFilters(updateFilterStatus: Boolean){
        when(updateFilterStatus){
            //Update the filter visibility and store the visibility state so we can keep it the same
            //after configuration changes
            true -> if (nearbyViewModel.getFilterVisibilityStatus()) {
                        hideFilterOptions()
                        nearbyViewModel.setFilterVisibilityStatus(false)
                    } else {
                        showFilterOptions()
                        nearbyViewModel.setFilterVisibilityStatus(true)
                    }
            //We do not update the filter status when we show/hide after configuration changes.
            false -> if (!nearbyViewModel.getFilterVisibilityStatus()) {
                        hideFilterOptions()
                    } else {
                        showFilterOptions()
                    }
        }
    }

    private fun showFilterOptions(){
        val animFadeIn: Animation = AnimationUtils.loadAnimation(this.context, android.R.anim.fade_in)
        animFadeIn.duration = 350

        nearbyBinding?.filterClear?.isVisible = true
        nearbyBinding?.filterClear?.startAnimation(animFadeIn)
        nearbyBinding?.filterRating0?.isVisible = true
        nearbyBinding?.filterRating0?.startAnimation(animFadeIn)
        nearbyBinding?.filterRating1?.isVisible = true
        nearbyBinding?.filterRating1?.startAnimation(animFadeIn)
        nearbyBinding?.filterRating2?.isVisible = true
        nearbyBinding?.filterRating2?.startAnimation(animFadeIn)
        nearbyBinding?.filterRating3?.isVisible = true
        nearbyBinding?.filterRating3?.startAnimation(animFadeIn)
        nearbyBinding?.filterRating4?.isVisible = true
        nearbyBinding?.filterRating4?.startAnimation(animFadeIn)
        nearbyBinding?.filterRating5?.isVisible = true
        nearbyBinding?.filterRating5?.startAnimation(animFadeIn)
    }

    private fun hideFilterOptions(){
        val animFadeOut: Animation = AnimationUtils.loadAnimation(this.context, android.R.anim.fade_out)
        animFadeOut.duration = 100

        nearbyBinding?.filterClear?.isVisible = false
        nearbyBinding?.filterClear?.startAnimation(animFadeOut)
        nearbyBinding?.filterRating0?.isVisible = false
        nearbyBinding?.filterRating0?.startAnimation(animFadeOut)
        nearbyBinding?.filterRating1?.isVisible = false
        nearbyBinding?.filterRating1?.startAnimation(animFadeOut)
        nearbyBinding?.filterRating2?.isVisible = false
        nearbyBinding?.filterRating2?.startAnimation(animFadeOut)
        nearbyBinding?.filterRating3?.isVisible = false
        nearbyBinding?.filterRating3?.startAnimation(animFadeOut)
        nearbyBinding?.filterRating4?.isVisible = false
        nearbyBinding?.filterRating4?.startAnimation(animFadeOut)
        nearbyBinding?.filterRating5?.isVisible = false
        nearbyBinding?.filterRating5?.startAnimation(animFadeOut)
    }

    private fun filterList(filter: String) {
        lifecycleScope.launch {
            nearbyViewModel.filterList(filter)?.collect {
                adapter.submitData(it)
            }
        }
    }

    private fun scrollToTop() {
        //https://git.io/JUsKp
        //Scroll to the top of the list
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { nearbyBinding?.listRecyclerView?.scrollToPosition(0) }
        }
    }

    //Called initially after first location load and then on each swipeRefresh
    private fun getEstablishments(getNewData: Boolean) {
        if (getNewData) {
            //Cancels previous job then create a new one to get data
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                nearbyViewModel.searchEstablishments().collectLatest {
                    adapter.submitData(it)
                }
            }
            //Hide SwipeRefresh ProgressBar
            swipeRefresh.isRefreshing = false

        } else {
            //Configuration change, so fetch current data
            lifecycleScope.launch {
                nearbyViewModel.getCurrentSearchResult()?.collectLatest {
                    adapter.submitData(it)
                }
            }
            //Filter the list if one was previously applied
            if (nearbyViewModel.getCurrentFilter() != "clear") {
                filterList(nearbyViewModel.getCurrentFilter())
            }
            //Hide SwipeRefresh ProgressBar
            swipeRefresh.isRefreshing = false
        }

        //Trigger Activity to hide ProgressBar
        fragmentVisibleListener?.onFragmentVisible()
        initAdapter()
    }

    private fun initAdapter() {
        //Display progressBar or retry button for loading of data or failure of loading
        nearbyBinding?.listRecyclerView?.adapter = adapter.withLoadStateHeaderAndFooter(
            header = NearbyLoadStateAdapter { adapter.retry() },
            footer = NearbyLoadStateAdapter { adapter.retry() }
        )

        //show / hide the header or footer views based on loading state
        adapter.addLoadStateListener { loadState ->
            // Only show the list if refresh succeeds.
            nearbyBinding?.listRecyclerView?.isVisible =
                loadState.source.refresh is LoadState.NotLoading
            // Show progress bar during initial load or refresh.
            nearbyBinding?.progressbarList?.isVisible =
                loadState.source.refresh is LoadState.Loading
            // Show the retry button if initial load or refresh fails.
            nearbyBinding?.retryButton?.isVisible = loadState.source.refresh is LoadState.Error
            // Show message if no results
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1) {
                recyclerView.isVisible = false
                no_results_text.isVisible = true
            } else {
                no_results_text.isVisible = false
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            //Permission not granted - request permissions
            requestPermissions()
        } else {
            locationServices.startLocationUpdates()
        }
    }

    override fun onDestroyView() {
        nearbyBinding = null
        super.onDestroyView()
    }

    //Check if permissions are granted
    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            this.requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Provide an additional rationale to the user if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale, android.R.string.ok) {
                // Request permission
                startLocationPermissionRequest()
            }

        } else {
            // Request permission
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }


    //Callback on completed permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            when {
                // User interaction was interrupted
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")

                // Permission granted.
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> {
                    locationServices.startLocationUpdates()
                    Log.i(TAG, "Permission granted")
                }

                // Permission denied.
                else -> {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings
                    ) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            this.view!!, getString(snackStrId),
            Snackbar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }
}