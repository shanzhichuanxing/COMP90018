package com.example.homepage

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.homepage.databinding.ActivityMapsBinding
import com.example.homepage.model.Place
import com.example.homepage.utils.BitmapHelper
import com.example.homepage.utils.MarkerInfoWindowAdapter
import com.example.homepage.utils.addressToLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker




class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val  rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open) }
    private val  rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close) }
    private val  fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom) }
    private val  toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom) }
    private var clicked = false
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val TAG = "MapsActivity"

    private lateinit var levelThreeBtn:View
    private lateinit var levelTwoBtn:View
    private lateinit var levelOneBtn:View
    private lateinit var layerButton:View

    private var data = mockData()
    var myMarkers = ArrayList<Marker>()

    private val alertOneIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.red)
        BitmapHelper.vectorToBitmap(this, R.drawable.tier1)
    }

    private val alertTwoIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.orange)
        BitmapHelper.vectorToBitmap(this, R.drawable.tier2)
    }

    private val alertThreeIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.blue)
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

        levelThreeBtn= findViewById(R.id.levelThreeBtn)
        levelTwoBtn= findViewById(R.id.levelTwoBtn)
        levelOneBtn= findViewById(R.id.levelOneBtn)
        layerButton= findViewById(R.id.layerButton)

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

    private fun addMarker(place: Place) {
        when (place.alert_level) {
            1 -> {
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .title(place.name)
                        .position(place.latLng)
                        .icon(alertOneIcon)
                )
                marker.tag = place
            }
            2 -> {
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .title(place.name)
                        .position(place.latLng)
                        .icon(alertTwoIcon)
                )
                marker.tag = place
            }
            3 -> {
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .title(place.name)
                        .position(place.latLng)
                        .icon(alertThreeIcon)
                )
                // Set place as the tag on the marker object so it can be referenced within
                // MarkerInfoWindowAdapter
                marker.tag = place
            }
        }
    }

    /**
     * Adds marker representations of the places list on the provided GoogleMap object
     */
    private fun addMarkers() {

        var places = ArrayList<Place>()

        data.forEach{ case ->
            var alert_level = 0
            when (case[3]) {
                "Tier 1" -> { alert_level = 1 }
                "Tier 2" -> { alert_level = 2 }
                "Tier 3" -> { alert_level = 3 }
            }
            places.add(Place(case[0], addressToLocation.getLocationFromAddress(this, case[0]), case[2], alert_level))
        }

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
    }

    private fun mockData(): List<List<String>> {
        //listOf("Site_title", "Site_state", "Site_address", "advice_title")
        return listOf(
            listOf(
                "Hardrock Climbing | Melbourne CBD",
                "VIC",
                "123 test road",
                "Tier 1"
            ),
            listOf(
                "EQ Tower",
                "VIC",
                "123 test road",
                "Tier 2"
            ),
            listOf(
                "BIG W Queen Victoria Village",
                "VIC",
                "123 test road",
                "Tier 3"
            )
        )
    }
}

