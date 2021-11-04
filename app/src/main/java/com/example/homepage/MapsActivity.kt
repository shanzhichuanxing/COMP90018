package com.example.homepage

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

import com.example.homepage.databinding.ActivityMapsBinding
import com.example.homepage.model.Place
import com.example.homepage.utils.BitmapHelper
import com.example.homepage.utils.MarkerInfoWindowAdapter
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_maps.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private val  rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open) }
    private val  rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close) }
    private val  fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom) }
    private val  toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom) }
    private var clicked = false;
    private val TAG = "MapsActivity"
    private var places = ArrayList<Place>()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var levelThreeBtn:View
    private lateinit var levelTwoBtn:View
    private lateinit var levelOneBtn:View
    private lateinit var layerButton:View
    private lateinit var traceMenuButton:View
    private lateinit var reCenterButton:View

    var isPermissionGranted = false

    var locationManager: LocationManager? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var userID: String? = null
    private var mCurrentLocation: Location? = null
    var myMarkers = ArrayList<Marker>()
    var myMarkerOptions = ArrayList<MarkerOptions>()

    private val alertOneIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(this, R.drawable.tier1)
    }

    private val alertTwoIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(this, R.drawable.tier2)
    }

    private val alertThreeIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(this, R.drawable.tier3)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().getReference("Trace")
        checkMyPermission()
        isGPSEnabled()

        if (isPermissionGranted)
        {
            val supportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            supportMapFragment!!.getMapAsync(this)
            supportMapFragment.getMapAsync {
                addMarkers()
                mMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
            }
        }

        levelThreeBtn= findViewById(R.id.levelThreeBtn)
        levelTwoBtn= findViewById(R.id.levelTwoBtn)
        levelOneBtn= findViewById(R.id.levelOneBtn)
        layerButton= findViewById(R.id.layerButton)
        reCenterButton= findViewById(R.id.location)

        traceMenuButton = findViewById(R.id.traceMenu)
        traceMenuButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MapsActivity, TraceActivity::class.java)
            intent.apply {
                putExtra("places", places)
            }
            startActivity(intent)
        })
        reCenterButton.setOnClickListener{
            if (mCurrentLocation == null) {
                Log.d("reCenterButton","Null current location")
                return@setOnClickListener
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation?.let { it1 ->
                LatLng(
                    it1.latitude, mCurrentLocation!!.longitude)
            },15f))
        }
        layerButton.setOnClickListener{
            onAddButtonClicked()
        }

        levelOneBtn.setOnClickListener{
            var tierMarkerList = ArrayList<Marker>()
            myMarkers.forEach { marker ->
                var place = marker.tag as Place
                if (place.alert_level == 1) {
                    marker.isVisible = !marker.isVisible
                    tierMarkerList.add(marker)
                }
            }
            if(tierMarkerList.size == 0){
                Toast.makeText(this, "No tier 1 exposure sites",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Showing tier 1 exposure sites",Toast.LENGTH_SHORT).show()
            rebounds(tierMarkerList)
        }
        levelTwoBtn.setOnClickListener{
            var tierMarkerList = ArrayList<Marker>()
            myMarkers.forEach { marker ->
                var place = marker.tag as Place
                if (place.alert_level == 2) {
                    marker.isVisible = !marker.isVisible
                    tierMarkerList.add(marker)
                }

            }

            if(tierMarkerList.size == 0){
                Toast.makeText(this, "No tier 2 exposure sites",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Showing tier 2 exposure sites",Toast.LENGTH_SHORT).show()
            rebounds(tierMarkerList)
        }
        levelThreeBtn.setOnClickListener{
            var tierMarkerList = ArrayList<Marker>()
            myMarkers.forEach { marker ->
                var place = marker.tag as Place
                if (place.alert_level == 3) {
                    marker.isVisible = !marker.isVisible
                    tierMarkerList.add(marker)
                }
            }
            if(tierMarkerList.size == 0){
                Toast.makeText(this, "No tier 3 exposure sites",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Showing tier 3 exposure sites",Toast.LENGTH_SHORT).show()
            rebounds(tierMarkerList)
        }
    }
    fun rebounds(tierMarkerList: ArrayList<Marker>){
        val b = LatLngBounds.Builder()
        for (m in tierMarkerList) {
            b.include(m.position)
        }

        val bounds = b.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 1500, 1000, 10)
        mMap.animateCamera(cu)
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {

        if(!clicked){
            levelOneBtn.visibility = View.VISIBLE
            levelTwoBtn.visibility = View.VISIBLE
            levelThreeBtn.visibility = View.VISIBLE
        }else{
            levelOneBtn.visibility = View.INVISIBLE
            levelTwoBtn.visibility = View.INVISIBLE
            levelThreeBtn.visibility = View.INVISIBLE
        }

    }

    private fun setAnimation(clicked: Boolean) {

        if(!clicked){
            levelOneBtn.startAnimation(fromBottom)
            levelTwoBtn.startAnimation(fromBottom)
            levelThreeBtn.startAnimation(fromBottom)
            layerButton.startAnimation(rotateOpen)
        }else{
            levelOneBtn.startAnimation(toBottom)
            levelTwoBtn.startAnimation(toBottom)
            levelThreeBtn.startAnimation(toBottom)
            layerButton.startAnimation(rotateClose)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = false
        // Add a marker in Sydney and move the camera
        val melbourne = LatLng(-37.8116, 144.9646)

        var myLocation = mCurrentLocation?.let { LatLng(it.latitude, mCurrentLocation!!.longitude) }
        if (myLocation==null){
            myLocation = melbourne
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,15f))
    }

    /**
     * Adds marker representations of the places list on the provided GoogleMap object
     */
    private fun addMarkers() {
        places = CaseAlert().getPlaces(this)!!;
        places.forEach{ place ->
            when (place.alert_level) {
                1 -> {
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .title(place.name)
                            .position(LatLng(place.lat, place.lng))
                            .icon(alertOneIcon)
                            .visible(false)
                    )
                    marker.tag = place
                    myMarkers.add(marker)

                }
                2 -> {
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .title(place.name)
                            .position(LatLng(place.lat, place.lng))
                            .icon(alertTwoIcon)
                            .visible(false)
                    )
                    marker.tag = place
                    myMarkers.add(marker)

                }
                3 -> {
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .title(place.name)
                            .position(LatLng(place.lat, place.lng))
                            .icon(alertThreeIcon)
                            .visible(false)

                    )
                    // Set place as the tag on the marker object so it can be referenced within
                    // MarkerInfoWindowAdapter
                    marker.tag = place
                    myMarkers.add(marker)


                }

            }
        }
        Log.i(TAG, "addMarkers completed")
    }


    @Throws(ParseException::class)
    private fun getGPSLocalTime(gpsTime: Long): Array<String> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = gpsTime
        @SuppressLint("SimpleDateFormat") val datef =
            SimpleDateFormat("yyyy-MM-dd")
        @SuppressLint("SimpleDateFormat") val timef =
            SimpleDateFormat("HH:mm:ss")
        val calendarTime = calendar.time
        val date = datef.format(calendarTime).toString()
        val time = timef.format(calendarTime)
        return arrayOf(date, time)
    }

    private fun checkMyPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    Toast.makeText(this@MapsActivity, "Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                    isPermissionGranted = true
                    getLocationUpdates()
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, "")
                    intent.data = uri
                    startActivity(intent)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }
    private fun isGPSEnabled(): Boolean {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?;
        var providerEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(providerEnabled == true){
            Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    private fun gotoLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
        mMap.moveCamera(cameraUpdate)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }
    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            30000,
            1f,
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        try {

            val array = getGPSLocalTime(location.time)
            val date = "2021-11-01"
            val time = array[1]
            val lat = location.latitude
            val lng = location.longitude
            mCurrentLocation = location
            Toast.makeText(
                this,
                "Location: " + location.latitude + ", " + location.longitude + ", " + time,
                Toast.LENGTH_SHORT
            ).show()

            mDatabase!!.child(userID!!).child(date).child(time).child("Lat").setValue(lat)
            mDatabase!!.child(userID!!).child(date).child(time).child("Lng").setValue(lng)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}

