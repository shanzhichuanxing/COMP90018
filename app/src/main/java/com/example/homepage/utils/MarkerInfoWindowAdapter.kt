package com.example.homepage.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.homepage.R
import com.example.homepage.model.Place
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker?): View? {
        // 1. Get tag
        val place = marker?.tag as? Place ?: return null

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(
            R.layout.marker_info_contents, null
        )
        view.findViewById<TextView>(
            R.id.title
        ).text = place.name
        view.findViewById<TextView>(
            R.id.address
        ).text = place.address
        view.findViewById<TextView>(
            R.id.alert_level
        ).text = place.alert_level.toString()

        return view
    }

    override fun getInfoWindow(marker: Marker?): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}