package com.example.homepage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.homepage.databinding.ActivityMapsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val  rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open) }
    private val  rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close) }
    private val  fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom) }
    private val  toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom) }
    private var clicked = false;
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var levelThreeBtn:View
    private lateinit var levelTwoBtn:View
    private lateinit var levelOneBtn:View
    private lateinit var layerButton:View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        levelThreeBtn= findViewById(R.id.levelThreeBtn)
        levelTwoBtn= findViewById(R.id.levelTwoBtn)
        levelOneBtn= findViewById(R.id.levelOneBtn)
        layerButton= findViewById(R.id.layerButton)

        layerButton.setOnClickListener{
            onAddButtonClicked()
        }

        levelOneBtn.setOnClickListener{
            Toast.makeText(this, "levelOne Clicked",Toast.LENGTH_SHORT).show()
        }
        levelTwoBtn.setOnClickListener{
            Toast.makeText(this, "levelTwo Clicked",Toast.LENGTH_SHORT).show()
        }
        levelThreeBtn.setOnClickListener{
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
            levelOneBtn.visibility = View.VISIBLE;
            levelTwoBtn.visibility = View.VISIBLE;
            levelThreeBtn.visibility = View.VISIBLE;
        }else{
            levelOneBtn.visibility = View.INVISIBLE;
            levelTwoBtn.visibility = View.INVISIBLE;
            levelThreeBtn.visibility = View.INVISIBLE;
        }

    }

    private fun setAnimation(clicked: Boolean) {

        if(!clicked){
            levelOneBtn.startAnimation(fromBottom);
            levelTwoBtn.startAnimation(fromBottom);
            levelThreeBtn.startAnimation(fromBottom)
            layerButton.startAnimation(rotateOpen)
        }else{
            levelOneBtn.startAnimation(toBottom);
            levelTwoBtn.startAnimation(toBottom);
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
}

