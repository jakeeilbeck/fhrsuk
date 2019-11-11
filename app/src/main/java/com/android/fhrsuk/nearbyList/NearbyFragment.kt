package com.android.fhrsuk.nearbyList

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.fhrsuk.BuildConfig
import com.android.fhrsuk.EstablishmentAdapter
import com.android.fhrsuk.R
import com.android.fhrsuk.models.EstablishmentDetail
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

private const val UPDATE_INTERVAL: Long = 10 * 1000  //10 secs
private const val FASTEST_INTERVAL: Long = 2000 //2 sec

private const val PERMISSIONS_REQUEST_CODE = 11
private const val TAG = "NearbyFragment"

class NearbyFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EstablishmentAdapter

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var fabUp: FloatingActionButton

    private lateinit var nearbyViewModel: NearbyViewModel

    private lateinit var locationRequest: LocationRequest

    private var firstCall: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        retainInstance = true
        nearbyViewModel = ViewModelProviders.of(this).get(NearbyViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_nearby_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        progressBar = view.findViewById(R.id.progressbar_list)
        showProgressBar(true)
        fabUp = view.findViewById(R.id.fab_up)
        fabUp.hide()

        adapter = EstablishmentAdapter(requireContext())
        recyclerView = view.findViewById(R.id.list_recyclerView) as RecyclerView
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
        Log.i(TAG, "onStart")
        super.onStart()

        if (!checkPermissions()) {
            //Permission not granted - request permissions
            showProgressBar(false)
            requestPermissions()
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {

        Log.i(TAG, "Location updates started")

        // Create the location request to start receiving updates
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = UPDATE_INTERVAL
        locationRequest.fastestInterval =
            FASTEST_INTERVAL

        getFusedLocationProviderClient(this.requireContext()).requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {

                    Log.i(
                        "NearbyFrag",
                        "Long: ${locationResult!!.lastLocation.longitude} Lat: ${locationResult.lastLocation.latitude}"
                    )

                    nearbyViewModel.setLocation(locationResult.lastLocation)
                    nearbyViewModel.init()

                    //Only the first request should be triggered by location updates
                    //subsequent requests are used on swipeRefresh
                    if (firstCall) {
                        init()
                        firstCall = false
                    }
                }
            },
            Looper.myLooper()
        )
    }

    //Called initially and on each swipeRefresh
    private fun init() {
        nearbyViewModel.itemPagedList.observe(this,
            Observer<PagedList<EstablishmentDetail>> { items ->
                items?.let {

                    swipeRefresh.isRefreshing = false
                    recyclerView.adapter = adapter
                    adapter.submitList(items)
                    adapter.notifyDataSetChanged()
                    showProgressBar(false)
                }
            })
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
                    startLocationUpdates()
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
