package com.example.homepage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.annotation.NonNull

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.homepage.databinding.ActivityMapsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TraceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var backButton: View
    private lateinit var calendar:CalendarView
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String

    private lateinit var year: String
    private lateinit var month: String
    private lateinit var dayOfMonth: String

    private lateinit var traceDays: DataSnapshot
    private lateinit var traceList: DataSnapshot
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
        Log.d("UserIDs:", userID)
        mDatabase = FirebaseDatabase.getInstance().getReference("Trace")

        var ref = FirebaseDatabase.getInstance().getReference("Trace").child(userID)

        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val traceList = dataSnapshot.child("Trace").getValue()
                traceDays = dataSnapshot //gets the location at different times of each day
                Log.d("TD----------------:", "traceDays: " + traceDays.getValue())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        ref.addListenerForSingleValueEvent(menuListener)




        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
            val msg = "Selected date is " + dayOfMonth + "/" + (month + 1) + "/" + year
            Toast.makeText(this@TraceActivity, msg, Toast.LENGTH_SHORT).show()

            this.year = year.toString()
            this.month = month.toString()
            this.dayOfMonth = dayOfMonth.toString()
            if (dayOfMonth < 10) {
                this.dayOfMonth = "0$dayOfMonth"
            }
            //read database
            Log.d(
                "onDateChangeListener",
                "got" + traceDays.child(year.toString() + "-" + (month + 1).toString() + "-" + this.dayOfMonth)
            )

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

}

