package com.example.homepage

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.homepage.databinding.ActivityMapsBinding
import com.example.homepage.model.Place
import com.example.homepage.utils.BitmapHelper
import com.example.homepage.utils.MarkerInfoWindowAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.Task

import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private val REQUEST_CHECK_SETTINGS = 0x1
    private val PERMISSIONS_FINE_LOCATION = 444
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var levelThreeBtn:View
    private lateinit var levelTwoBtn:View
    private lateinit var levelOneBtn:View
    private lateinit var layerButton:View
    private lateinit var traceMenuButton:View
    private lateinit var reCenterButton:View
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var isPermissionGranted = false
    var isProviderEnabled = false

    var fab: FloatingActionButton? = null
    private val mLocationClient: FusedLocationProviderClient? = null
    var locSearch: EditText? = null
    var searchIcon: ImageButton? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    var locationManager: LocationManager? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var userID: String? = null
    //fab = findViewById(R.id.fab);
    private var mCurrentLocation: Location? = null
    var myMarkers = ArrayList<Marker>()


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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.getMapAsync {
            addMarkers()
            mMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        }

        //fab = findViewById(R.id.fab);
//        locSearch = findViewById(R.id.et_search);
//        searchIcon = findViewById(R.id.search_icon);
        //adding haoyue
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().getReference("Trace")
        checkMyPermission()
        isGPSEnabled()


        if (isPermissionGranted)
        {
            if (isProviderEnabled) {
                val supportMapFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment?
                supportMapFragment!!.getMapAsync(this)
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
            myMarkers.forEach { marker ->
                var place = marker.tag as Place
                if (place.alert_level == 1) {
                    marker.isVisible = !marker.isVisible
                }
            }
            Toast.makeText(this, "levelOne Clicked",Toast.LENGTH_SHORT).show()
        }
        levelTwoBtn.setOnClickListener{
            myMarkers.forEach { marker ->
                var place = marker.tag as Place
                if (place.alert_level == 2) {
                    marker.isVisible = !marker.isVisible
                }
            }
            Toast.makeText(this, "levelTwo Clicked",Toast.LENGTH_SHORT).show()
        }
        levelThreeBtn.setOnClickListener{
            myMarkers.forEach { marker ->
                var place = marker.tag as Place
                if (place.alert_level == 3) {
                    marker.isVisible = !marker.isVisible
                }
            }
            Toast.makeText(this, "levelThree Clicked",Toast.LENGTH_SHORT).show()
        }
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

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                            .position(place.latLng)
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
                            .position(place.latLng)
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
                            .position(place.latLng)
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

//    private fun writeLocToDB(location:Location){
//        val array: Array<String> = getGPSLocalTime(location.getTime())
//        val date = array[0]
//        val time = array[1]
//        val lat: Double = location.getLatitude()
//        val lng: Double = location.getLongitude()
//        mDatabase!!.child(userID!!).child(date).child(time).child("Lat").setValue(lat)
//        mDatabase!!.child(userID!!).child(date).child(time).child("Lng").setValue(lng)
//    }
//    override fun onResume() {
//        super.onResume()
//        if (isPermissionGranted) startLocationUpdates()
//    }

//    private fun startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        fusedLocationClient.requestLocationUpdates(mLocationRequest,
//            mLocationCallback,
//            Looper.getMainLooper())
//    }

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

