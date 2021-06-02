package com.originWzh.gpstrackdemo.bean

data class LocationInfo(val oriLon: Double, val oriLat: Double) {
    private var lat: Double = oriLon
    private var lon: Double = oriLat

    fun getLat(): Double {
        return lat
    }

    fun getLon(): Double {
        return lon
    }
}