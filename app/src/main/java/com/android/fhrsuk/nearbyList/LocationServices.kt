package com.android.fhrsuk.nearbyList

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

private const val UPDATE_INTERVAL: Long = 10 * 1000  //10 secs
private const val FASTEST_INTERVAL: Long = 2000 //2 sec

private const val TAG = "LocationServices"

class LocationServices(private val context: Context) {

    val location = MutableLiveData<Location>()

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {

        Log.i(TAG, "Location updates started")

        // Create the location request to start receiving updates
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = UPDATE_INTERVAL
        locationRequest.fastestInterval = FASTEST_INTERVAL

        LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {

                    location.value = locationResult?.lastLocation

                }
            },
            Looper.myLooper()
        )
    }
}