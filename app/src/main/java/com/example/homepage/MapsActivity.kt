package com.example.homepage

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast



import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.homepage.databinding.ActivityMapsBinding
import com.example.homepage.model.Place
import com.example.homepage.utils.BitmapHelper
import com.example.homepage.utils.MarkerInfoWindowAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
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
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


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
    var isProviderEnabled = false

    var fab: FloatingActionButton? = null
    private val mLocationClient: FusedLocationProviderClient? = null
    var locSearch: EditText? = null
    var searchIcon: ImageButton? = null
    private val mLocationCallback: LocationCallback? = null
    private val mLocationRequest: LocationRequest? = null
    var locationManager: LocationManager? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var userID: String? = null
    //fab = findViewById(R.id.fab);

    // search bar




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

        // search box////////////////////////








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

        traceMenuButton = findViewById(R.id.traceMenu)
        traceMenuButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MapsActivity, TraceActivity::class.java)
            startActivity(intent)
        })

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


        // Add a marker in Sydney and move the camera
        val melbourne = LatLng(-37.8116, 144.9646)
        mMap.addMarker(MarkerOptions().position(melbourne).title("Marker in Melbourne"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne,15f))

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

}

