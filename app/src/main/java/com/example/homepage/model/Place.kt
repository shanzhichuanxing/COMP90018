package com.example.homepage.model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Place (
    val name: String,
    val latLng: LatLng,
    val address: String,
    val alert_level: Int
) : Serializable