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
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.fhrsuk.BuildConfig
import com.android.fhrsuk.R
import com.android.fhrsuk.RecyclerViewAdapter
import com.android.fhrsuk.databinding.FragmentNearbyListBinding
import com.android.fhrsuk.models.EstablishmentDetail
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

private const val PERMISSIONS_REQUEST_CODE = 11
private const val TAG = "NearbyFragment"

class NearbyFragment : Fragment(R.layout.fragment_nearby_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var nearbyViewModel: NearbyViewModel

    private lateinit var locationServices: LocationServices

    private var firstCall: Boolean = true

    private var fragmentNearbyListBinding: FragmentNearbyListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        nearbyViewModel = ViewModelProvider(this).get(NearbyViewModel::class.java)

        //Observer for location updates
        val locationObserver = Observer<Location> { newLocation ->

            nearbyViewModel.setLocation(newLocation)
            nearbyViewModel.init()

            //Only the first request should be triggered by location update
            if (firstCall) {
                init()
                firstCall = false
            }
        }

        locationServices = LocationServices(this.requireActivity())
        locationServices.startLocationUpdates()
        locationServices.location.observe(this, locationObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNearbyListBinding.bind(view)
        fragmentNearbyListBinding = binding

        swipeRefresh = binding.swipeRefresh
        progressBar = binding.progressbarList
        val fabUp: FloatingActionButton = binding.fabUp

        //show/hide progressBar based on retrofit loading status
        val loadingStateObserver = Observer<Int> { currentState ->
            if (currentState == 0) {
                showProgressBar(false)
            } else if (currentState == 1) {
                showProgressBar(true)
            }
        }
        nearbyViewModel.nearbyLoadingState.observe(viewLifecycleOwner, loadingStateObserver)

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
    }

    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            //Permission not granted - request permissions
            showProgressBar(false)
            requestPermissions()
        } else {
            locationServices.startLocationUpdates()
        }
    }

    override fun onDestroyView() {
        fragmentNearbyListBinding = null
        super.onDestroyView()
    }

    //Called initially and on each swipeRefresh
    //Refreshes views with new data
    private fun init() {
        nearbyViewModel.itemPagedList.observe(viewLifecycleOwner,
            Observer<PagedList<EstablishmentDetail>> { items ->
                items?.let {

                    swipeRefresh.isRefreshing = false
                    recyclerView.adapter = adapter
                    adapter.submitList(items)
                    adapter.notifyDataSetChanged()
                }
            })
        showProgressBar(false)
    }

    private fun showProgressBar(setVisible: Boolean) {
        if (setVisible) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
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
                    showProgressBar(true)
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