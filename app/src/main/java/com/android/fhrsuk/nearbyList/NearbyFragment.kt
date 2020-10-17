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

        //fabUp will only be visible after the user has started scrolling
        fabUp.hide()

        adapter = RecyclerViewAdapter(requireContext())

        recyclerView = binding.listRecyclerView
        initAdapter()

        //stops 'blinking' effect when item is clicked
        recyclerView.itemAnimator?.changeDuration = 0

        // Show/hide the fab button after scrolled passed ~1 page of results
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

        swipeRefresh.setOnRefreshListener {
            //Update ViewModel with latest location then search
            nearbyViewModel.setLocation(location)
            getEstablishments()
        }

        //https://git.io/JUsKp
        //Scroll to top of list on refresh
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect{binding.listRecyclerView.scrollToPosition(0)}
        }

        nearbyBinding?.retryButton?.setOnClickListener { adapter.retry() }
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
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount <1){
                recyclerView.isVisible = false
                no_results_text.isVisible = true
            }else{
                no_results_text.isVisible = false
            }
        }
    }

    //Called initially and on each swipeRefresh
    private fun getEstablishments() {

        //Cancels previous job then create a new one to get data
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            nearbyViewModel.searchEstablishments().collectLatest {
                adapter.submitData(it)
            }
        }

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