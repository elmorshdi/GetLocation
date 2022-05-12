package com.elmorshdi.getlocation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlin.math.roundToInt


class MapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    // private lateinit var loca:Location
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
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
        val myLocation = Location("provider")
        getLastKnownLocation {
            myLocation.latitude=it.latitude
            myLocation.longitude=it.longitude
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.latitude,it.longitude))
                    .title("Marker in myLocation")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude), 12.0f))

        }

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.setOnMapLongClickListener {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it))
            mMap.addMarker(MarkerOptions().position(LatLng(myLocation.latitude,myLocation.longitude)))

            mMap.addPolygon(
                PolygonOptions()
                    .add(it)
                    .add(LatLng(myLocation.latitude,myLocation.longitude))
                    .strokeColor(Color.RED)
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12.0f))
            val result = FloatArray(1)
            Location.distanceBetween(myLocation.latitude,myLocation.longitude,it.latitude,it.longitude,result)
            Toast.makeText(applicationContext,"${result[0].roundToInt()} meter",Toast.LENGTH_LONG).show()
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(
            "LOCATION",
            "latitude:${p0.position.latitude} , longitude:${p0.position.longitude}"
        )
        startActivity(intent)
        return true
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(onLocationAvailable: (Location) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null)
                onLocationAvailable(location)
            else
                createLocationRequest(onLocationAvailable)
        }
    }

    private fun createLocationRequest(onLocationAvailable: (Location) -> Unit) {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    onLocationAvailable(location)
                }
            }
        }
        requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (locationCallback != null && locationRequest != null)
            fusedLocationClient.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
                Looper.myLooper()!!
            )
    }
}