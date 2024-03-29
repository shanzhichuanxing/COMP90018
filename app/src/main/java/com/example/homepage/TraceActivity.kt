package com.example.homepage

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.homepage.model.Place
import com.example.homepage.utils.BitmapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.gms.maps.model.*


class TraceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var backButton: View
    private lateinit var calendar:CalendarView
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String

    private lateinit var year: String
    private lateinit var month: String
    private lateinit var dayOfMonth: String

    private lateinit var traceDays: DataSnapshot
    private lateinit var attachments: ArrayList<Place>
    private lateinit var places: ArrayList<Place>

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

        setContentView(R.layout.trace_maps)
        backButton = findViewById(R.id.traceMenuBack)
        calendar = findViewById(R.id.calendarView)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        backButton.setOnClickListener {
            finish()
        }
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.currentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().getReference("Trace")

        var ref = FirebaseDatabase.getInstance().getReference("Trace").child(userID)

        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                traceDays = dataSnapshot //gets the location at different times of each day
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        ref.addListenerForSingleValueEvent(menuListener)

        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
//            val msg = "Selected date is " + dayOfMonth + "/" + (month + 1) + "/" + year
//            Toast.makeText(this@TraceActivity, msg, Toast.LENGTH_SHORT).show()
            mMap.clear()
            addMarkers()
            this.year = year.toString()
            this.month = month.toString()
            this.dayOfMonth = dayOfMonth.toString()
            if (dayOfMonth < 10) {
                this.dayOfMonth = "0$dayOfMonth"
            }
            //read database
            if(!(this::traceDays.isInitialized)){
                Toast.makeText(this@TraceActivity, "Database isn't connected", Toast.LENGTH_SHORT).show()
                return@setOnDateChangeListener;
            }
            val days = traceDays.child(year.toString() + "-" + (month + 1).toString() + "-" + this.dayOfMonth)

            if(days.value==null){
//                Toast.makeText(this@TraceActivity, "Theres no recorded trace master!EXITTTINGG", Toast.LENGTH_SHORT).show()
                return@setOnDateChangeListener;
            }
            val markers = days!!.children // all the markers in a single day

            var markerList:ArrayList<Marker> = ArrayList()
            var posList:ArrayList<LatLng> = ArrayList()
            //add the markers at each (time, location) combo for each child
            markers.forEach {
                val strMarker = it.value.toString()
                val Lng = strMarker.substringAfter("Lng=").substringBefore(",").toDouble()
                val Lat = strMarker.substringAfter("Lat=").substringBefore("}").toDouble()
                val pos = LatLng(Lat,Lng)
                val marker = mMap.addMarker(MarkerOptions().position(pos).title(it.key.toString()))
                //fitting all markers
                markerList.add(marker)
                posList.add(pos)
            }
            //Line
            val polyline1 = mMap.addPolyline(
                PolylineOptions()
            .clickable(true)
            .addAll(
                posList))
            polyline1.tag = "A"
            //resizing to fit all markers
            MapsActivity().rebounds(markerList,mMap)

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

        attachments = intent.getSerializableExtra("places") as ArrayList<Place>
        places = attachments.toCollection(ArrayList())

        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = false
        // Add a marker in Sydney and move the camera
        val melbourne = LatLng(-37.8116, 144.9646)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne,15f))
        addMarkers()
    }
    /**
     * Adds marker representations of the places list on the provided GoogleMap object
     */
    private fun addMarkers() {

        places.forEach { place ->
           when (place.alert_level) {
               1 -> {
                   mMap.addMarker(
                       MarkerOptions()
                           .title(place.name)
                           .position(LatLng(place.lat, place.lng))
                           .icon(alertOneIcon)
                           .visible(true)
                   )
               }
               2 -> {
                   mMap.addMarker(
                       MarkerOptions()
                           .title(place.name)
                           .position(LatLng(place.lat, place.lng))
                           .icon(alertTwoIcon)
                           .visible(true)
                   )
               }
               3 -> {
                   mMap.addMarker(
                       MarkerOptions()
                           .title(place.name)
                           .position(LatLng(place.lat, place.lng))
                           .icon(alertThreeIcon)
                           .visible(true)
                   )
               }
           }
        }
        Log.i("AddMarker", "addMarkers completed")
    }
}

