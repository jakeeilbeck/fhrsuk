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
    private var firstCall: Boolean = true

    private lateinit var location: Location

    var fragmentVisibleListener: FragmentVisibleListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nearbyViewModel = ViewModelProvider(this, Injection.provideNearbyViewModelFactory())
            .get(NearbyViewModel::class.java)

        //Observer for location updates
        val locationObserver = Observer<Location> { newLocation ->

            location = newLocation

            //Only the initial request of the session should be triggered by a location update
            //Subsequent updates triggered by Swipe Refresh
            if (firstCall) {
                nearbyViewModel.setLocation(newLocation)
                getEstablishments()
                firstCall = false
            }
        }

        //Initialise location updates and observe latest results
        locationServices = LocationServices(this.requireActivity())
        locationServices.location.observe(this, locationObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNearbyListBinding.bind(view)
        nearbyBinding = binding
        swipeRefresh = binding.swipeRefresh
        val fabUp: FloatingActionButton = binding.fabUp
        val fabFilter: FloatingActionButton = binding.fabFilter
        val filterClear = binding.filterClear
        val filter0 = binding.filterRating0
        val filter1 = binding.filterRating1
        val filter2 = binding.filterRating2
        val filter3 = binding.filterRating3
        val filter4 = binding.filterRating4
        val filter5 = binding.filterRating5

        //fabUp will only be visible after the user has started scrolling
        fabUp.hide()

        adapter = RecyclerViewAdapter(requireContext())

        recyclerView = binding.listRecyclerView
        initAdapter()

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

        //scroll to top of list on click
        fabUp.setOnClickListener { lifecycleScope.launch { recyclerView.scrollToPosition(0) } }

        //Fade animations for filter options
        val animFadeIn: Animation = AnimationUtils.loadAnimation(this.context, android.R.anim.fade_in)
        animFadeIn.duration = 350
        val animFadeOut: Animation = AnimationUtils.loadAnimation(this.context, android.R.anim.fade_out)
        animFadeOut.duration = 100

        //Show/Hide filter options and apply fade animation
        var expandedState = false
        fabFilter.setOnClickListener {
            if (expandedState) {
                filterClear.isVisible = false
                filterClear.startAnimation(animFadeOut)
                filter0.isVisible = false
                filter0.startAnimation(animFadeOut)
                filter1.isVisible = false
                filter1.startAnimation(animFadeOut)
                filter2.isVisible = false
                filter2.startAnimation(animFadeOut)
                filter3.isVisible = false
                filter3.startAnimation(animFadeOut)
                filter4.isVisible = false
                filter4.startAnimation(animFadeOut)
                filter5.isVisible = false
                filter5.startAnimation(animFadeOut)
                expandedState = false
            } else {
                filterClear.isVisible = true
                filterClear.startAnimation(animFadeIn)
                filter0.isVisible = true
                filter0.startAnimation(animFadeIn)
                filter1.isVisible = true
                filter1.startAnimation(animFadeIn)
                filter2.isVisible = true
                filter2.startAnimation(animFadeIn)
                filter3.isVisible = true
                filter3.startAnimation(animFadeIn)
                filter4.isVisible = true
                filter4.startAnimation(animFadeIn)
                filter5.isVisible = true
                filter5.startAnimation(animFadeIn)
                expandedState = true
            }
        }

        filterClear.setOnClickListener {
            lifecycleScope.launch {
                nearbyViewModel.filterList("clear")?.collect {
                    adapter.submitData(it)
                }
            }
            scrollToTop(binding)
        }

        filter0.setOnClickListener {
            lifecycleScope.launch {
                nearbyViewModel.filterList("0")?.collect {
                    adapter.submitData(it)
                }
            }
            scrollToTop(binding)
        }

        filter1.setOnClickListener {
            lifecycleScope.launch {
                nearbyViewModel.filterList("1")?.collect {
                    adapter.submitData(it)
                }
            }
            scrollToTop(binding)
        }

        filter2.setOnClickListener {
            lifecycleScope.launch {
                nearbyViewModel.filterList("2")?.collect {
                    adapter.submitData(it)
                }
            }
            scrollToTop(binding)
        }

        filter3.setOnClickListener {
            lifecycleScope.launch {
                nearbyViewModel.filterList("3")?.collect {
                    adapter.submitData(it)
                }
            }
            scrollToTop(binding)
        }

        filter4.setOnClickListener {
            lifecycleScope.launch {
                nearbyViewModel.filterList("4")?.collect {
                    adapter.submitData(it)
                }
            }
            scrollToTop(binding)
        }

        filter5.setOnClickListener {
            lifecycleScope.launch {
                nearbyViewModel.filterList("5")?.collect {
                    adapter.submitData(it)
                }
            }
            scrollToTop(binding)
        }

        swipeRefresh.setOnRefreshListener {
            //Update ViewModel with latest location then search
            nearbyViewModel.setLocation(location)
            getEstablishments()
        }

        scrollToTop(binding)

        nearbyBinding?.retryButton?.setOnClickListener { adapter.retry() }
    }

    private fun scrollToTop(binding: FragmentNearbyListBinding) {
        //https://git.io/JUsKp
        //Scroll to the top of the list
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.listRecyclerView.scrollToPosition(0) }
        }
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
            nearbyBinding?.listRecyclerView?.isVisible = loadState.source.refresh is LoadState.NotLoading
            // Show progress bar during initial load or refresh.
            nearbyBinding?.progressbarList?.isVisible = loadState.source.refresh is LoadState.Loading
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

    //Called initially after first location load and then on each swipeRefresh
    private fun getEstablishments() {

        //Cancels previous job then create a new one to get data
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            nearbyViewModel.searchEstablishments().collectLatest {
                adapter.submitData(it)
            }
        }
        //Trigger Activity to hide ProgressBar
        fragmentVisibleListener?.onFragmentVisible()
        //Hide SwipeRefresh ProgressBar
        swipeRefresh.isRefreshing = false
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
            showSnackbar(R.string.permission_rationale, android.R.string.ok, View.OnClickListener {
                // Request permission
                startLocationPermissionRequest()
            })

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
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        })
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