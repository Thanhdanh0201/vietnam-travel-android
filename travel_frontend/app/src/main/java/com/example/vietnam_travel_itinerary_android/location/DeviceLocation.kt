package com.example.vietnam_travel_itinerary_android.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private fun Location.isUsableFix(): Boolean =
    latitude != 0.0 || longitude != 0.0

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.fetchBestLocation(): Location? {
    return try {
        lastLocation.await()?.takeIf { it.isUsableFix() }
            ?: getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await()?.takeIf { it.isUsableFix() }
    } catch (_: Exception) {
        null
    }
}

suspend fun reverseGeocodeLocality(context: Context, lat: Double, lng: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale.forLanguageTag("vi-VN"))
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(lat, lng, 1)
            list?.firstOrNull()?.let { a ->
                a.locality?.takeIf { it.isNotBlank() }
                    ?: a.subAdminArea?.takeIf { it.isNotBlank() }
                    ?: a.adminArea?.takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }
