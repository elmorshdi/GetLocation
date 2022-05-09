package com.elmorshdi.getlocation

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapActivity : AppCompatActivity() , OnMapReadyCallback ,
    GoogleMap.OnMarkerClickListener
{
  // private lateinit var loca:Location
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var locationRequest : LocationRequest?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        getLastKnownLocation {
            val sydney = LatLng(it.latitude, it.longitude)
              //loca.set(it)
            mMap.addMarker(
                    MarkerOptions()
                        .position(sydney)
                        .title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,8.0f))

        }

            mMap.setOnMarkerDragListener(object : OnMarkerDragListener {
    override fun onMarkerDragStart(marker: Marker) {
        // TODO Auto-generated method stub
    }

    override fun onMarkerDragEnd(marker: Marker) {
        // TODO Auto-generated method stub
        mMap.addMarker(
            MarkerOptions()
                .position(marker.position)
                .title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position,8.0f))

    }

    override fun onMarkerDrag(marker: Marker) {
        // TODO Auto-generated method stub
    }
})

}
    override fun onMarkerClick(p0: Marker): Boolean {
        val intent= Intent(applicationContext,MainActivity::class.java)
        intent.putExtra("LOCATION","latitude:${p0.position.latitude} , longitude:${p0.position.longitude}")
        startActivity(intent)
        return true
    }
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(onlocationAvailable: (Location) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null)
                onlocationAvailable(location)
            else
                createLocationRequest(onlocationAvailable)
        }
    }

    private fun createLocationRequest(onlocationAvailable: (Location) -> Unit) {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for(location in result.locations){
                    onlocationAvailable(location)
                }
            }
        }
        requestLocationUpdates()
    }
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(){
        if(locationCallback !=null && locationRequest !=null)
            fusedLocationClient.requestLocationUpdates(locationRequest!!, locationCallback!!, Looper.myLooper()!! )
    }
}