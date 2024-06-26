package com.arquette.locationdemo

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
//import android.location.LocationRequest
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import com.arquette.locationdemo.dto.LocationDetails
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import java.util.Locale

class LocationLiveData(var context: Context): LiveData<LocationDetails>(){
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun onActive(){
        super.onActive()
        //get last known location
        if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location.also {
                setLocationData(it)
                getAddressFromLocation(it)
            }
        }
        startLocationUpdates()
    }
    private val locationCallback = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult ?: return
            for(location in locationResult.locations){
                setLocationData(location)
                getAddressFromLocation(location)
            }
        }
    }

    companion object {
        val ONE_MINUTE: Long = 60000 //1 min in miliseconds
        val locationRequest: LocationRequest =
                LocationRequest.Builder(ONE_MINUTE)
                        .setMinUpdateIntervalMillis(ONE_MINUTE)
                        .setPriority(PRIORITY_HIGH_ACCURACY)
                        .build()
    }

    private val geocoderCallback = object : Geocoder.GeocodeListener {
        override fun onGeocode(addresses: MutableList<Address>) {
            postValue(
                LocationDetails(
                    addresses.get(0).longitude.toString(),
                    addresses.get(0).latitude.toString(),
                    addresses.get(0).getAddressLine(0)
                )
            )
        }

        override fun onError(errorMessage:String?){
            super.onError(errorMessage)
            postValue(LocationDetails("","",errorMessage?: "Error Geocoding"))
        }
    }//geocoder callback

    private fun getAddressFromLocation(location:Location?){
        val geocoder = Geocoder(context, Locale.getDefault())
        location?.let{
            geocoder.getFromLocation(location.latitude, location.longitude, 1, geocoderCallback)
        }
    }


    internal fun startLocationUpdates(){

        if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper())
    }

    private fun setLocationData(location: Location?){
        location?.let{location ->
            value = LocationDetails(location.longitude.toString(), location.latitude.toString(), "")
        }
    }

    override fun onInactive(){
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
