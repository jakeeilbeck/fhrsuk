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
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.fhrsuk.BuildConfig
import com.android.fhrsuk.Injection
import com.android.fhrsuk.R
import com.android.fhrsuk.RecyclerViewAdapter
import com.android.fhrsuk.databinding.FragmentNearbyListBinding
import com.android.fhrsuk.utils.toVisibility
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val PERMISSIONS_REQUEST_CODE = 11
private const val TAG = "NearbyFragment"

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class NearbyFragment : Fragment(R.layout.fragment_nearby_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var nearbyViewModel: NearbyViewModel

    private lateinit var locationServices: LocationServices

    private var firstCall: Boolean = true

    private var searchJob: Job? = null

    private var nearbyBinding: FragmentNearbyListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nearbyViewModel = ViewModelProvider(this, Injection.provideViewModelFactory())
            .get(NearbyViewModel::class.java)

        //Observer for location updates
        val locationObserver = Observer<Location> { newLocation ->

            //Update ViewModel with latest location
            nearbyViewModel.setLocation(newLocation)

            //Only the initial request should be triggered by location update
            //Subsequent updates triggered by Swipe Refresh
            if (firstCall) {
                init()
                firstCall = false
            }
        }

        //Start location updates and observe latest results
        locationServices = LocationServices(this.requireActivity())
        //locationServices.startLocationUpdates()
        locationServices.location.observe(this, locationObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNearbyListBinding.bind(view)
        nearbyBinding = binding
        swipeRefresh = binding.swipeRefresh
        progressBar = binding.progressbarList
        val fabUp: FloatingActionButton = binding.fabUp

        //fabUp will only be visible after the user has started scrolling
        fabUp.hide()

        adapter = RecyclerViewAdapter(requireContext())
        recyclerView = binding.listRecyclerView

        //stops 'blinking' effect when item is clicked
        recyclerView.itemAnimator?.changeDuration = 0

        recyclerView.layoutManager = LinearLayoutManager(context)

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
        fabUp.setOnClickListener {
            recyclerView.scrollToPosition(0)
        }

        swipeRefresh.setOnRefreshListener {
            init()
        }

        nearbyBinding?.retryButton?.setOnClickListener { adapter.retry() }
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

    //Called initially and on each swipeRefresh
    @InternalCoroutinesApi
    private fun init() {
        nearbyBinding?.listRecyclerView?.adapter = adapter.withLoadStateHeaderAndFooter(
            header = NearbyLoadStateAdapter { adapter.retry() },
            footer = NearbyLoadStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh !is LoadState.NotLoading) {
                nearbyBinding?.listRecyclerView?.visibility = View.GONE
                nearbyBinding?.progressbarList?.visibility =
                    toVisibility(loadState.refresh is LoadState.Loading)
                nearbyBinding?.retryButton?.visibility =
                    toVisibility(loadState.refresh is LoadState.Error)
            } else {
                nearbyBinding?.listRecyclerView?.visibility = View.VISIBLE
                nearbyBinding?.progressbarList?.visibility = View.GONE
                nearbyBinding?.retryButton?.visibility = View.GONE
            }
        }

        //Cancel previous job then create a new one
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            nearbyViewModel.searchRepo().collectLatest {
                adapter.submitData(it)
            }
        }

        swipeRefresh.isRefreshing = false
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