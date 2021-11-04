package com.example.homepage.model

import java.io.Serializable

data class Place (
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val alert_level: Int
) : Serializable