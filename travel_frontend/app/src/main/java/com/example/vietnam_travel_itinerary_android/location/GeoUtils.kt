package com.example.vietnam_travel_itinerary_android.location

import com.example.vietnam_travel_itinerary_android.data.model.Place
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {

    private const val EARTH_RADIUS_M = 6_371_000.0

    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val dφ = Math.toRadians(lat2 - lat1)
        val dλ = Math.toRadians(lng2 - lng1)
        val a = sin(dφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(dλ / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    fun hasValidCoords(lat: Double, lng: Double): Boolean =
        abs(lat) > 1e-5 && abs(lng) > 1e-5

    fun nearestPlace(places: Iterable<Place>, userLat: Double, userLng: Double): Place? =
        places
            .filter { hasValidCoords(it.lat, it.lng) }
            .minByOrNull { distanceMeters(it.lat, it.lng, userLat, userLng) }
}
